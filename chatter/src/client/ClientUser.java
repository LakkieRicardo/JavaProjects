package client;

public class ClientUser {
    
    public String username;
    public final ServerConnection server;

    public ClientUser(String username, ServerConnection server) {
        this.username = username;
        this.server = server;
    }

    public String toString() {
        return String.format("%s", username);
    }

    /**
     * Turns a {@link server.Server} provided user data into a {@link ClientUser}
     * @param serverData The individual user's data which the server returned
     * @param server The server from which the data was collected
     * @return Generated {@link ClientUser}
     */
    public static ClientUser interpretServerData(String serverData, ServerConnection server) {
        return new ClientUser(serverData, server);
    }

}
