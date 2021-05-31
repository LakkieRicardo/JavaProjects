package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.*;

public class ServerUser {

    public String username = "";
    public final Socket socket;
    public final BufferedReader reader;
    public final PrintWriter writer;
    public long latestPing = 0;
    public long latestPingTime = 0;
    public long lastClientPing = -1;

    public ServerUser(Socket socket, BufferedReader reader, PrintWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public String toString() {
        return String.format("%s", username);
    }

}
