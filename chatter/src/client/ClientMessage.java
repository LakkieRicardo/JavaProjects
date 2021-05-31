package client;

public class ClientMessage {
    
    public final ClientUser sender;
    public final String content;

    public ClientMessage(ClientUser sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String toString() {
        return String.format("[sender=%s,content=%s]", sender, content);
    }

}
