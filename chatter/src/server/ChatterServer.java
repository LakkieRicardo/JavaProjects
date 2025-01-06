package server;

import java.net.*;
import java.io.*;
import java.util.*;
import util.ServerDataCodes;

/**
 * Contains all of the sockets representing client connections and the server socket. Manages resources for listening
 * to clients, sending messages, and handling client requests.
 */
public class ChatterServer {
    
    public final Thread connectionListenerThread;
    public final Thread pingThread;

    public final ServerSocket serverSocket;
    private final ServerResourceManager res;

    private final List<Thread> clientListenerThreads = new ArrayList<Thread>(); // TODO convert this into a thread pool
    public final List<ServerUser> connectedUsers = new ArrayList<ServerUser>();
    private final List<ChatterServerMessage> messageLog = new ArrayList<ChatterServerMessage>();

    public ChatterServer(int port) throws IOException {
        res = new ServerResourceManager("ServerData");
        System.out.println("Starting server on port " + port + "...");
        serverSocket = new ServerSocket(port);
        connectionListenerThread = new Thread(this::listenForConnections);
        pingThread = new Thread(this::pingLoop);

        connectionListenerThread.start();
        pingThread.start();
    }

    public void listenForConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(() -> listenForClient(clientSocket));
                clientListenerThreads.add(thread);
                thread.start();
            } catch (IOException e) {
                System.err.println("Warning: error while listening for new clients: " + e.getMessage());
            }
        }
    }

    public List<ServerUser> getConnectedUsers() {
        return connectedUsers;
    }

    public boolean usernameExists(String username) {
        for (ServerUser user : connectedUsers) {
            if (user.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastMessage(String message) {
        for (ServerUser user : connectedUsers) {
            user.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.BROADCAST_CODE + message);
            user.writer.flush();
        }
    }

    private void handleUsernameRequest(BufferedReader reader, PrintWriter writer, ServerUser user, String requestData) {
        if (!requestData.matches("^[a-zA-Z0-9]{1,16}$")) {
            writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + ServerDataCodes.USERNAME_CODE + requestData + ";Invalid username format");
            writer.flush();
            return;
        }
        if (usernameExists(requestData)) {
            writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + ServerDataCodes.USERNAME_CODE + requestData + ";Username already exists");
            writer.flush();
            return;
        }
        System.out.printf("Changing user with name %s to name %s\n", user.username, requestData);
        broadcastMessage(String.format("User %s changed their name to %s", user.username, requestData));
        user.username = requestData;
        writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_OK_CODE + ServerDataCodes.USERNAME_CODE);
        writer.flush();
    }

    private void handleUserListRequest(BufferedReader reader, PrintWriter writer) {
        StringBuilder result = new StringBuilder();
        for (ServerUser user : connectedUsers) {
            result.append(';');
            result.append(user.toString());
        }
        writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.USER_LIST_CODE + new String(result).substring(1));
        writer.flush();
    }

    private void handleMessageRequest(BufferedReader reader, PrintWriter writer, ServerUser user, String requestData) {
        if (requestData.contains("\n") || requestData.length() == 0) {
            return;
        }
        messageLog.add(new ChatterServerMessage(user, requestData));
        System.out.println("Received message from " + user.username + ": " + requestData);
        connectedUsers.toString();
        for (ServerUser userAll : connectedUsers) {
            userAll.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.MESSAGE_CODE + user.toString() + ";" + requestData);
            userAll.writer.flush();
        }
    }

    private void listenForClient(Socket socket) {
        BufferedReader reader;
        PrintWriter writer;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String username;
        try {
            // Wait for username
            while (!reader.ready()) { }
            username = reader.readLine();
            if (usernameExists(username)) {
                writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + "Username already exists");
                writer.flush();
                socket.close();
                return;
            }
            if (!username.matches("^[a-zA-Z0-9]{1,16}$")) {
                writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + "Username does not match format");
                writer.flush();
                socket.close();
                return;
            }
            if (res.getBannedUsers().contains(socket.getInetAddress())) {
                writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_ERR_CODE + "User is banned");
                writer.flush();
                socket.close();
                return;
            }
            writer.println(ServerDataCodes.RESPONSE_CODE + ServerDataCodes.RESPONSE_OK_CODE);
            writer.flush();
        } catch (Exception e) {
            System.err.printf("Unable to connect to user @ %s, error message: %s\n", socket.getInetAddress(), e.getMessage());
            return;
        }
        ServerUser user = new ServerUser(socket, reader, writer);
        user.username = username;
        connectedUsers.add(user);
        System.out.printf("Listening for client at %s with username %s\n", socket.getInetAddress().getHostName(), user.username);
        // At least 1 other client is listening(besides who just connected)
        if (connectedUsers.size() > 1) {
            for (ServerUser userAll : connectedUsers) {
                if (userAll.username.equals(user.username)) {
                    continue;
                }
                userAll.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.NEW_USER_CODE + user.username);
                userAll.writer.flush();
            }
        }
        while (!serverSocket.isClosed()) {
            try {
                if (user.lastClientPing != -1) {
                    if (System.currentTimeMillis() - user.lastClientPing > 10000) {
                        System.out.printf("%s did not ping for 10+ seconds; timing out...\n", user);
                        connectedUsers.remove(user);
                        socket.close();
                        reader.close();
                        writer.close();
                        return;
                    }
                }
                if (!reader.ready()) {
                    continue;
                }
                String message = reader.readLine();

                if (message.startsWith(ServerDataCodes.REQUEST_CODE + ServerDataCodes.USERNAME_CODE)) {
                    String requestData = message.substring((ServerDataCodes.REQUEST_CODE + ServerDataCodes.USERNAME_CODE).length());
                    handleUsernameRequest(reader, writer, user, requestData);
                }
                if (message.startsWith(ServerDataCodes.REQUEST_CODE + ServerDataCodes.USER_LIST_CODE)) {
                    handleUserListRequest(reader, writer);
                }
                if (message.startsWith(ServerDataCodes.REQUEST_CODE + ServerDataCodes.MESSAGE_CODE)) {
                    String requestData = message.substring((ServerDataCodes.REQUEST_CODE + ServerDataCodes.MESSAGE_CODE).length());
                    handleMessageRequest(reader, writer, user, requestData);
                }
                if (message.startsWith(ServerDataCodes.UPDATE_CODE + ServerDataCodes.DISCONNECT_CODE)) {
                    System.out.printf("Disconnecting user %s...\n", user.username);
                    connectedUsers.remove(user);
                    for (ServerUser userAll : connectedUsers) {
                        userAll.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.DISCONNECT_CODE + user.username);
                        userAll.writer.flush();
                    }
                    break;
                }
                if (message.startsWith(ServerDataCodes.PING_CODE)) {
                    user.latestPing = System.currentTimeMillis() - user.latestPingTime;
                }
                if (message.equals(ServerDataCodes.CLIENT_PING_CODE)) {
                    user.writer.println(ServerDataCodes.CLIENT_PING_CODE);
                    user.writer.flush();
                    user.lastClientPing = System.currentTimeMillis();
                }
            } catch (Exception e) {
                System.err.println("Warning: error while listening for client: " + e.getMessage());
                connectedUsers.remove(user);
                break;
            }
        }
    }

    private void pingLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Thread.sleep(1000);
                for (ServerUser user : connectedUsers) {
                    user.latestPingTime = System.currentTimeMillis();
                    user.writer.println(ServerDataCodes.PING_CODE);
                    user.writer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ServerResourceManager getResourceManager() {
        return res;
    }

    public void shutDownServer() throws IOException, InterruptedException {
        serverSocket.close();
        for (Thread t : clientListenerThreads) {
            t.join();
        }
        connectionListenerThread.join();
        pingThread.join();
        getResourceManager().writeOut();
    }

}
