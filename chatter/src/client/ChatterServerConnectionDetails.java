package client;

import java.net.InetAddress;

public class ChatterServerConnectionDetails {

    public InetAddress host;
    public String username;
    public int port;

    public ChatterServerConnectionDetails() { this.port = -1;}

    public ChatterServerConnectionDetails(InetAddress host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String toString() {
        return String.format("[host=%s,port=%s,username=%s]", host, port, username);
    }
        
}
