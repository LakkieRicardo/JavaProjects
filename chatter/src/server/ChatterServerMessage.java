package server;

public class ChatterServerMessage {
    
    public final ServerUser sender;
    public final String content;

    public ChatterServerMessage(ServerUser sender, String content) {
        this.sender = sender;
        this.content = content;
    }

}
