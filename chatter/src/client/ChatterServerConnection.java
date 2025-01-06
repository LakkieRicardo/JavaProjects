package client;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import org.fusesource.jansi.Ansi;

import util.ServerDataCodes;

public class ChatterServerConnection implements Flushable, Closeable {
    
    private String username;
    public final Socket socket;
    public final PrintWriter writer;
    public final BufferedReader reader;
    public final Thread listenerThread, pingThread;
    private List<ChatterClientUser> connectedUsersCache;

    public long lastPingTime = 0, lastPing = 0;

    public ChatterServerConnection(InetAddress target, int port, String username) throws UnknownHostException, IOException {
        this.username = username;
        socket = new Socket(target, port);
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Special message: initial username. Other messages must be formatted
        writer.println(username);
        writer.flush();
        String serverResponse = reader.readLine();
        if (serverResponse.startsWith(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE)) {
            String errorMessage = serverResponse.substring((ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE).length());
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + errorMessage);
            writer.close();
            reader.close();
            socket.close();
            listenerThread = null;
            pingThread = null;
            return;
        }
        connectedUsersCache = Arrays.asList(new ChatterClientUser(username, this));
        listenerThread = new Thread(this::listen);
        listenerThread.start();
        pingThread = new Thread(this::pingLoop);
        pingThread.start();
    }

    public String getUsername() {
        return this.username;
    }

    private void pingLoop() {
        while (!isClosed()) {
            try {
                Thread.sleep(1000);
                lastPingTime = System.currentTimeMillis();
                writer.println(ServerDataCodes.CLIENT_PING_CODE);
                writer.flush();
                getConnectedUsers(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void listen() {
        while (!isClosed()) {
            try {
                if (!reader.ready()) {
                    continue;
                }
                String serverResponse = reader.readLine();
                if (!serverResponse.equals(ServerDataCodes.PING_CODE) && !serverResponse.equals(ServerDataCodes.CLIENT_PING_CODE)) {
                    System.out.println("Received: " + serverResponse);
                }
                if (serverResponse.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.USER_LIST_CODE)) {
                    String userListData = serverResponse.substring((ServerDataCodes.RESPONSE_CODE + ServerDataCodes.USER_LIST_CODE).length());
                    if (!userListData.contains(";")) {
                        connectedUsersCache = Arrays.asList(ChatterClientUser.interpretServerData(userListData, this));
                    }
                    List<ChatterClientUser> userList = new ArrayList<ChatterClientUser>();
                    for (String user : userListData.split(";")) {
                        userList.add(ChatterClientUser.interpretServerData(user, this));
                    }
                    connectedUsersCache = userList;
                }
                if (serverResponse.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.MESSAGE_CODE)) {
                    String updateData = serverResponse.substring((ServerDataCodes.UPDATE_CODE + ServerDataCodes.MESSAGE_CODE).length());
                    int separatorIndex = updateData.indexOf(";");
                    if (separatorIndex == -1) {
                        continue;
                    }
                    ChatterClientUser sender = ChatterClientUser.interpretServerData(updateData.substring(0, separatorIndex), this);
                    if (sender.username.equals(username)) {
                        // Sender is self, don't repeat
                        continue;
                    }
                    String content = updateData.substring(separatorIndex + 1);
                    ChatterClientMessage message = new ChatterClientMessage(sender, content);
                    ChatterClientUI.showMessage(message);
                }
                if (serverResponse.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.NEW_USER_CODE)) {
                    String updateData = serverResponse.substring((ServerDataCodes.UPDATE_CODE + ServerDataCodes.NEW_USER_CODE).length());
                    getConnectedUsers(true);
                    ChatterClientUI.showMessage(updateData + " just connected! Say hi!");
                }
                if (serverResponse.equals(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_OK_CODE + ServerDataCodes.USERNAME_CODE)) {
                    ChatterClientUI.getFrame().setTitle("Messenger client: " + username);
                }
                if (serverResponse.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.BROADCAST_CODE)) {
                    String updateData = serverResponse.substring((ServerDataCodes.UPDATE_CODE + ServerDataCodes.BROADCAST_CODE).length());
                    ChatterClientUI.showMessage(Ansi.ansi().fgYellow().a("[Server] ").fgBlack().a(updateData).toString());
                }
                if (serverResponse.equals(ServerDataCodes.UPDATE_CODE + ServerDataCodes.SERVER_SHUTDOWN_CODE)) {
                    JOptionPane.showMessageDialog(null, "Server is shutting down!");
                    ChatterClientUI.getFrame().dispose();
                    close();
                    System.exit(0);
                }
                if (serverResponse.equals(ServerDataCodes.PING_CODE)) {
                    writer.println(ServerDataCodes.PING_CODE);
                    writer.flush();
                }
                if (serverResponse.equals(ServerDataCodes.CLIENT_PING_CODE)) {
                    lastPing = System.currentTimeMillis() - lastPingTime;
                }
                if (serverResponse.startsWith(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + ServerDataCodes.USERNAME_CODE)) {
                    String errorMessage = serverResponse.substring((ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + ServerDataCodes.USERNAME_CODE).length());
                    String[] errorMessageParts = errorMessage.split(";");
                    username = errorMessageParts[0];
                    ChatterClientUI.showMessage(Ansi.ansi().fgRed().a("Failed to change username: " + errorMessageParts[1]).fgBlack().toString());
                }
                if (serverResponse.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.DISCONNECT_CODE)) {
                    String updateMessage = serverResponse.substring((ServerDataCodes.UPDATE_CODE + ServerDataCodes.DISCONNECT_CODE).length());
                    ChatterClientUI.showMessage(Ansi.ansi().fgYellow().a("[Server] ").fgBlack().a(String.format("User %s has disconnected", updateMessage)).toString());
                }
                if (serverResponse.equals(ServerDataCodes.UPDATE_CODE + ServerDataCodes.BANNED_CODE)) {
                    ChatterClientUI.showMessage(Ansi.ansi().fgRed().a("Server has banned you").toString());
                    close();
                    System.exit(0);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    /**
     * Sends request to server to update nickname
     * @param newUsername
     */
    public void updateUsername(String newUsername) throws IOException {
        getSelfUser(false).username = newUsername;
        username = newUsername;
        String updateMessage = ServerDataCodes.REQUEST_CODE + ServerDataCodes.USERNAME_CODE + newUsername;
        writer.println(updateMessage);
        writer.flush();
    }

    /**
     * Provides a list of connected users
     * @param updateCache Whether or not to update the user list cache
     * @return List of connected users from cache
     * @throws IOException If the server responded with an error
     */
    public List<ChatterClientUser> getConnectedUsers(boolean updateCache) throws IOException {
        if (updateCache) {
            // Make request to server
            writer.println(ServerDataCodes.REQUEST_CODE + ServerDataCodes.USER_LIST_CODE);
            writer.flush();
        }
        return connectedUsersCache;
    }

    /**
     * Retrieves the user connected to this server
     * @param updateUserListCache Whether to update user list cache
     * @return The {@link ChatterClientUser} connected to this server
     * @throws IOException If the server throws an error; only if updating cache
     */
    public ChatterClientUser getSelfUser(boolean updateUserListCache) throws IOException {
        for (ChatterClientUser user : getConnectedUsers(updateUserListCache)) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void sendMessage(String message) throws IOException {
        if (message.contains("\n")) {
            throw new IOException("Invalid message");
        }
        writer.println(ServerDataCodes.REQUEST_CODE + ServerDataCodes.MESSAGE_CODE + message);
        writer.flush();
    }

    public void flush() {
        writer.flush();
    }

    public void close() {
        try {
            // Send closing message
            writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.DISCONNECT_CODE);
            writer.flush();
            // Close IO
            writer.close();
            socket.close();
            reader.close();
            listenerThread.join(5000);
            pingThread.join(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

}
