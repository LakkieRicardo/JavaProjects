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
    
    public final ServerSocket serverSocket;
    private final ServerResourceManager res;

    private final List<Thread> clientListenerThreads = new ArrayList<Thread>(); // TODO convert this into a thread pool
    private final List<ServerUser> connectedUsers = new ArrayList<ServerUser>();
    private final List<ChatterServerMessage> messageLog = new ArrayList<ChatterServerMessage>();

    public ChatterServer(int port) throws IOException {
        res = new ServerResourceManager("ServerData");
        System.out.println("Starting server on port " + port + "...");
        serverSocket = new ServerSocket(port);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int port;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            System.out.print("Enter port : ");
            port = Integer.parseInt(scanner.nextLine());
        }
        ChatterServer server = new ChatterServer(port);
        Thread connectionListenerThread = new Thread(server::listenForConnections);
        connectionListenerThread.start();
        Thread pingThread = new Thread(server::pingLoop);
        pingThread.start();
        System.out.println("Type \"stop\" to stop and \"help\" for help");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("stop")) {
                break;
            }
            if (input.equals("help")) {
                System.out.println("Commands: stop, help, list_users, broadcast <message>, ping, ban <user/address>, unban <address> list_bans");
                continue;
            }
            if (input.equals("list_users")) {
                System.out.println("Connected users: " + server.getConnectedUsers());
            }
            if (input.startsWith("broadcast")) {
                if (!input.contains(" ")) {
                    System.out.println("Usage: broadcast <message>");
                    continue;
                }
                String content = input.substring("broadcast ".length());
                server.broadcastMessage(content);
            }
            if (input.startsWith("ping")) {
                if (!input.matches("ping [a-zA-Z0-9]{1,16}$")) {
                    if (server.connectedUsers.size() == 0) {
                        System.out.println("No connected users");
                        continue;
                    }
                    StringBuilder result = new StringBuilder();
                    result.append("All user pings: ");
                    for (ServerUser user : server.connectedUsers) {
                        result.append(String.format("(%s, %s ms), ", user.username, user.latestPing));
                    }
                    String resultString = new String(result);
                    System.out.println(resultString.substring(0, resultString.length() - 2));
                } else {
                    String userInput = input.substring("ping ".length());
                    boolean foundUser = false;
                    for (ServerUser user : server.connectedUsers) {
                        if (userInput.equals(user.username)) {
                            System.out.printf("%s ping: %s ms\n", userInput, user.latestPing);
                            foundUser = true;
                        }
                        break;
                    }
                    if (!foundUser) {
                        System.out.println("Unable to find user " + userInput);
                    }
                    continue;
                }
            }
            if (input.startsWith("ban ")) {
                String commandArg = input.substring("ban ".length());
                try {
                    InetAddress address = InetAddress.getByName(commandArg);
                    System.out.printf("Banning any user with address %s\n", address);
                    if (!server.getResourceManager().addBannedUser(address)) {
                        System.out.println("Error banning user with address" + address);
                    }
                } catch (Exception e) {
                    ServerUser user = null;
                    for (ServerUser userAll : server.getConnectedUsers()) {
                        if (userAll.username.equals(commandArg)) {
                            user = userAll;
                            break;
                        }
                    }
                    if (user == null) {
                        System.out.printf("%s could not be recognized; not banning\n", commandArg);
                        continue;
                    }
                    System.out.printf("Banning user %s with address %s\n", user.username, user.socket.getInetAddress());
                    if (!server.getResourceManager().addBannedUser(user.socket.getInetAddress())) {
                        System.out.println("Error banning user " + user.username);
                        continue;
                    }
                    user.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.BANNED_CODE);
                    user.writer.flush();
                    user.socket.close();
                    server.getConnectedUsers().remove(user);
                    server.broadcastMessage("Server has banned user " + user.username);
                }
                continue;
            }
            if (input.startsWith("unban ")) {
                String commandArg = input.substring("unban ".length());
                try {
                    InetAddress address = InetAddress.getByName(commandArg);
                    server.getResourceManager().removeBannedUser(address);
                } catch (Exception e) {
                    System.out.println("Could not recognize address: " + commandArg);
                }
                continue;
            }
            if (input.equals("list_bans")) {
                System.out.println("Banned users: " + server.getResourceManager().getBannedUsers());
                continue;
            }
        }
        for (ServerUser user : server.connectedUsers) {
            user.writer.println(ServerDataCodes.UPDATE_CODE + ServerDataCodes.SERVER_SHUTDOWN_CODE);
            user.writer.flush();
        }
        server.serverSocket.close();
        scanner.close();
        for (Thread t : server.clientListenerThreads) {
            t.join();
        }
        connectionListenerThread.join();
        pingThread.join();
        server.getResourceManager().writeOut();
    }

    public ServerResourceManager getResourceManager() {
        return res;
    }

}
