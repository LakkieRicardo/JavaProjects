package server;

import java.io.IOException;
import java.util.Scanner;

public class ChatterServerMain {
    
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
        ChatterServerCLI.runServerCli(server, scanner);
        server.shutDownServer();
        scanner.close();
    }

}
