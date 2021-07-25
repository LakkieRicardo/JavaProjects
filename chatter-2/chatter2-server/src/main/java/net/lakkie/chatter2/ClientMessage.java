package net.lakkie.chatter2;

public class ClientMessage {
    public final String type, content;
    public final String[] args;
    /**
     * The message this object was parsed from. May or may not be set
     */
    private String originalMessage = null;

    public ClientMessage(String type, String content, String[] args)
    {
        this.type = type;
        this.content = content;
        this.args = args;
        this.originalMessage = "c2/" + type + " " + content;
    }

    /**
     * Parses the arguments from <code>content</code>
     * @param type Type of message sent
     * @param content Contents of the message excluding the <code>c2/TYPE</code>
     */
    public ClientMessage(String type, String content)
    {
        this.type = type;
        this.content = content;
        this.args = parseMessageArguments(content);
        this.originalMessage = "c2/" + type + " " + content;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("c2/");
        out.append(type);
        if (!content.isEmpty()) out.append(" ");
        out.append(content);
        return new String(out);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientMessage)
        {
            ClientMessage that = (ClientMessage) obj;
            boolean bType = this.type.equals(that.type);
            boolean bContent = this.content.equals(that.content);
            boolean bArgs = this.args.length == that.args.length;
            if (bArgs) {
                for (int i = 0; i < this.args.length; i++)
                {
                    if (this.args[i].equals(that.args[i]))
                        bArgs = false;
                }
            }
            return bType && bContent && bArgs;
        }
        else
        {
            return obj.equals(this);
        }
    }

    public String getOriginalMessage()
    {
        return originalMessage;
    }

    public static String parseMessageType(String message)
    {
        int messageTypeBegin = "c2/".length(), messageTypeEnd = message.indexOf(" ");
        return message.substring(messageTypeBegin, messageTypeEnd);
    }

    public static String parseMessageContent(String message)
    {
        return parseMessageContent(parseMessageType(message), message);
    }

    public static String parseMessageContent(String messageType, String message)
    {
        int contentIndex = "c2/".length() + messageType.length() + 1; // 1 for the space between type and content
        if (contentIndex >= message.length())
            return "";
        return message.substring(contentIndex);
    }

    public static String[] parseMessageArguments(String messageContent)
    {
        if (messageContent.isEmpty())
            return new String[0];
        if (!messageContent.contains("; "))
            return new String[] { messageContent };
        String[] args = messageContent.split("; ");
        return args;
    }

    public static ClientMessage parseMessage(String message, int maxArguments) throws MessageArgumentOverflowException
    {
        String type = parseMessageType(message);
        String content = parseMessageContent(type, message);
        String[] args = parseMessageArguments(content);
        if (maxArguments == -1 ? false : args.length > maxArguments) throw new MessageArgumentOverflowException();
        ClientMessage clientMessage = new ClientMessage(type, content, args);
        clientMessage.originalMessage = message;
        return clientMessage;
    }

    public static ClientMessage parseMessage(String message)
    {
        try
        {
            return parseMessage(message, -1);
        }
        catch (MessageArgumentOverflowException e)
        {
            e.printStackTrace(); // Should not happen
            return null;
        }
    }

    public static class MessageArgumentOverflowException extends Exception { }
    
}