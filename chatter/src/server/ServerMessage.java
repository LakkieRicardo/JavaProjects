package server;

public class ServerMessage {
    
    public final ServerUser sender;
    public final String content;

    public ServerMessage(ServerUser sender, String content) {
        this.sender = sender;
        this.content = content;
    }

}
