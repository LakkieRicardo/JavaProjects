package client;

import java.net.UnknownHostException;

public class ChatterClientMain {
    
    public static void main(String[] args) {
        ChatterClient client;
        try {
            client = ChatterClient.createClientFromMainArgs(args);
        } catch (UnknownHostException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Failed to get connection details: " + e.getMessage());
            System.exit(2);
            return;
        }
        try {
            ChatterClientUI.createFrameFromClient(client);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while connected to server: " + e.getMessage());
        }
    }

}
