package client;

public class ChatterClientMessage {
    
    public final ChatterClientUser sender;
    public final String content;

    public ChatterClientMessage(ChatterClientUser sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String toString() {
        return String.format("[sender=%s,content=%s]", sender, content);
    }

}
