package client;

import java.net.InetAddress;

public class ConnectionDetails {

    public InetAddress host;
    public String username;
    public int port;

    public ConnectionDetails() { this.port = -1;}

    public ConnectionDetails(InetAddress host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String toString() {
        return String.format("[host=%s,port=%s,username=%s]", host, port, username);
    }
        
}
