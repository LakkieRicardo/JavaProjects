package client;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.*;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import client.commands.CommandManager;

public class Client {
    
    private ServerConnection server;

    public Client(InetAddress host, int port, String username) {
        try {
            server = new ServerConnection(host, port, username);
            if (server.socket.isClosed()) {
                return;
            }
            server.getConnectedUsers(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Client(ConnectionDetails details) {
        this(details.host, details.port, details.username);
    }

    public ServerConnection getServer() {
        return server;
    }

    public void quit() {
        try {
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        AnsiConsole.systemInstall();
        
        ConnectionDetails details = new ConnectionDetails();

        boolean cliInput = false;
        if (args.length == 1) {
            if (args[0].equals("cli")) {
                cliInput = true;
                // Interactive CLI input
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
            if (args[0].equals("--help") || args[0].equals("-help") || args[0].equals("-h") || args[0].equals("--h")) {
                System.out.println(Ansi.ansi().a("Usage:").newline().cursorRight(4).cursorDown(1).a("-help\tdisplay this help information").newline());
                System.out.println(Ansi.ansi().cursorRight(4).a("cli\t\tInput connection details w/ command line interface").newline());
                System.out.println(Ansi.ansi().cursorRight(4).a("-details <host> <port> <username>\tInput connection details w/ command line interface").newline());
                System.exit(0);
            }
        } else if (args.length == 4 && args[0].equals("-details")) {
            cliInput = true;
            // CLI args input
            details.host = InetAddress.getByName(args[1]);
            details.port = Integer.parseInt(args[2]);
            details.username = args[3];
        } else {
            // GUI input
            details = ClientRender.getConnectionDetails();
        }

        System.out.println("Connecting with details: " + details + "...");
        CommandManager.initCommands();
        Client client = new Client(details);
        if (client.getServer().isClosed()) {
            System.exit(2);
        }
        if (client.server == null && !cliInput) {
            JOptionPane.showMessageDialog(null, "Failed to connect to server with details: " + details);
            System.exit(1);
        }
        ClientRender.init(client, client.server);
        ClientRender.getFrame().addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent e) {
                client.quit();
                System.exit(0);
            }
            public void windowActivated(WindowEvent e) { }
            public void windowClosed(WindowEvent e) { }
            public void windowDeactivated(WindowEvent e) { }
            public void windowDeiconified(WindowEvent e) { }
            public void windowIconified(WindowEvent e) { }
            public void windowOpened(WindowEvent e) { }
        });
    }

}
