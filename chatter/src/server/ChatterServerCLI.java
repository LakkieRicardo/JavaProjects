package server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import util.ServerDataCodes;

public class ChatterServerCLI {
    
    private ChatterServerCLI() { }

    /**
     * Handles user input and sends those commands to the server.
     * @throws IOException If an I/O error is sent from the server.
     */
    public static void runServerCli(ChatterServer server, Scanner scanner) throws IOException {
        System.out.println("Type \"stop\" to stop and \"help\" for help");
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("stop")) {
                break;
            }
            if (input.equals("help")) {
                System.out.println("Commands: stop, help, list_users, broadcast <message>, ping, ban <user/address>, unban <address>, list_bans");
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
    }

}
