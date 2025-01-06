package client;

import java.net.*;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import client.commands.CommandManager;

public class ChatterClient {
    
    public ChatterServerConnection server;

    public ChatterClient(InetAddress host, int port, String username) {
        try {
            server = new ChatterServerConnection(host, port, username);
            if (server.socket.isClosed()) {
                return;
            }
            server.getConnectedUsers(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatterClient(ChatterServerConnectionDetails details) {
        this(details.host, details.port, details.username);
    }

    public ChatterServerConnection getServer() {
        return server;
    }

    public void quit() {
        try {
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getConnectionDetailsCli(ChatterServerConnectionDetails details) throws UnknownHostException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        details.username = scanner.nextLine();
        System.out.print("Enter host: ");
        String hostInput = scanner.nextLine();
        details.host = InetAddress.getByName(hostInput);
        System.out.print("Enter port: ");
        details.port = Integer.parseInt(scanner.nextLine());
        scanner.close();
    }

    private static void displayHelp() {
        System.out.println(Ansi.ansi().a("Usage:").newline().cursorRight(4).cursorDown(1).a("-help\tdisplay this help information").newline());
        System.out.println(Ansi.ansi().cursorRight(4).a("cli\t\tInput connection details w/ command line interface").newline());
        System.out.println(Ansi.ansi().cursorRight(4).a("-details <host> <port> <username>\tInput connection details w/ command line interface").newline());
    }

    private static void parseConnectionArgs(ChatterServerConnectionDetails details, String[] args) throws UnknownHostException {
        details.host = InetAddress.getByName(args[1]);
        details.port = Integer.parseInt(args[2]);
        details.username = args[3];
    }

    private static boolean getConnectionDetails(ChatterServerConnectionDetails details, String[] args) throws UnknownHostException, IllegalArgumentException {
        if (args.length == 1) {
            if (args[0].equals("cli")) {
                // Interactive CLI input
                getConnectionDetailsCli(details);
                return true;
            } else if (args[0].equals("--help") || args[0].equals("-help") || args[0].equals("-h") || args[0].equals("--h")) {
                displayHelp();
                System.exit(0);
                return false;
            } else {
                throw new IllegalArgumentException("Invalid argument: " + args[0]);
            }
        } else if (args.length == 4 && args[0].equals("-details")) {
            // CLI args input
            parseConnectionArgs(details, args);
            return true;
        } else {
            // GUI input
            ChatterServerConnectionDetails uiDetails = ChatterClientUI.getConnectionDetails();
            details.host = uiDetails.host;
            details.port = uiDetails.port;
            details.username = uiDetails.username;
            return false;
        }
    }

    public static ChatterClient createClientFromMainArgs(String[] args) throws UnknownHostException, IllegalArgumentException {
        AnsiConsole.systemInstall();
        ChatterServerConnectionDetails details = new ChatterServerConnectionDetails();
        boolean cliInput = getConnectionDetails(details, args);

        System.out.println("Connecting with details: " + details + "...");
        CommandManager.initCommands();
        ChatterClient client = new ChatterClient(details);
        if (client.getServer() == null || client.getServer().isClosed()) {
            System.err.println("Failed to connect to server");
            System.exit(2);
        }
        if (client.server == null) {
            if (cliInput) {
                System.err.println("Failed to connect to server with details: " + details);
                System.exit(1);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to connect to server with details: " + details);
                System.exit(1);
            }
        }
        return client;
    }

}
