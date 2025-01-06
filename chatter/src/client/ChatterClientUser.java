package client;

public class ChatterClientUser {
    
    public String username;
    public final ChatterServerConnection server;

    public ChatterClientUser(String username, ChatterServerConnection server) {
        this.username = username;
        this.server = server;
    }

    public String toString() {
        return String.format("%s", username);
    }

    /**
     * Turns a {@link server.ChatterServer} provided user data into a {@link ChatterClientUser}
     * @param serverData The individual user's data which the server returned
     * @param server The server from which the data was collected
     * @return Generated {@link ChatterClientUser}
     */
    public static ChatterClientUser interpretServerData(String serverData, ChatterServerConnection server) {
        return new ChatterClientUser(serverData, server);
    }

}
