package net.lakkie.chatter2;

public class ChatMessage {
    
    private String value;
    public final ServerUser sender;
    public final long timestamp;
    
    public ChatMessage(ServerUser sender, String value, long timestamp)
    {
        this.sender = sender;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue()
    {
        return value;
    }

}
