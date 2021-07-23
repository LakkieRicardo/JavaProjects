package net.lakkie.chatter2;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import net.lakkie.chatter2.ServerUser.ServerUserState;

public class C2Server extends WebSocketServer {

    public final Map<WebSocket, ServerUser> connectedUsers = new HashMap<WebSocket, ServerUser>();

    public final int serverID;
    public String serverName;

    public C2Server(int port, int serverID, String serverName)
    {
        super(new InetSocketAddress(port));
        this.serverID = serverID;
        this.serverName = serverName;
    }

    public void handleUserConnection(ServerUser user, ClientMessage message)
    {
        if (message.args.length == 1)
        {
            String providedUsername = message.args[0];
            if (providedUsername.length() < ServerProperties.MIN_USERNAME_LENGTH || providedUsername.length() > ServerProperties.MAX_USERNAME_LENGTH)
            {
                user.conn.send(new ClientMessage("ERROR", "Invalid username length", this).toString());
                return;
            }
            for (String illegalUsernameString : ServerProperties.ILLEGAL_USERNAME_STRINGS)
            {
                if (providedUsername.contains(illegalUsernameString)) // check if contains string
                {
                    user.conn.send(new ClientMessage("ERROR", "Username contains illegal string", this).toString());
                    return;
                }
            }
            user.username = providedUsername;
            user.state = ServerUserState.CONNECTED;
            user.conn.send(new ClientMessage("ACKNOWLEDGE", serverName, this).toString());
        }
        else
            user.conn.send(new ClientMessage("ERROR", "Invalid connect command!", this).toString());
    }

    public void handleUserPing(ServerUser user, ClientMessage message)
    {
        if (message.args.length == 0)
        {
            user.timeOfLastPing = System.currentTimeMillis();
            user.conn.send(new ClientMessage("PING", "" + serverID, this).toString());
        }
        else if (message.args.length == 1)
        {
            try
            {
                long timestamp = Long.parseLong(message.args[0]);
                long currentTime = System.currentTimeMillis();
                user.timeOfLastPing = currentTime;
                user.lastPingTime = currentTime - timestamp;
                user.conn.send(new ClientMessage("PING", currentTime + "; " + serverID, this).toString());
            } catch (NumberFormatException e)
            {
                user.conn.send(new ClientMessage("ERROR", "Invalid timestamp!", this).toString());
            }
        }
    }

    public void handleUserDisconnect(ServerUser user, ClientMessage message, String disconnectType)
    {
        user.state = ServerUserState.DISCONNECTED;
        user.conn.send(new ClientMessage("ACKNOWLEDGE", "", new String[0]).toString());
        broadcast("c2/DISCONNECT " + disconnectType + "; " + user.username);
    }

    public void handleSuddenDisconnect(ServerUser user)
    {
        broadcast("c2/DISCONNECT lost_connection; " + user.username);
    }

    public void handleMessage(ServerUser user, ClientMessage message)
    {
        if (message.type.equals("CONNECT"))
        {
            if (user.state == ServerUserState.CONNECTING)
                handleUserConnection(user, message);
            else
                user.conn.send(new ClientMessage("ERROR", "Connect command must be sent!", this).toString());
        }
        else if (message.type.equals("DISCONNECT"))
        {
            if (user.state == ServerUserState.CONNECTED)
                handleUserDisconnect(user, message, "disconnect");
            else if (user.state == ServerUserState.CONNECTING)
                user.state = ServerUserState.DISCONNECTED; // If never connected, do not broadcast to all users
            else
                user.conn.send(new ClientMessage("ERROR", "Cannot disconnect when not connected", this).toString());
        }
        else if (message.type.equals("PING"))
        {
            handleUserPing(user, message);
        }
    }
    
    public String parseMessageType(String message)
    {
        int messageTypeBegin = "c2/".length(), messageTypeEnd = message.indexOf(" ");
        return message.substring(messageTypeBegin, messageTypeEnd);
    }

    public String parseMessageContent(String message)
    {
        return parseMessageContent(parseMessageType(message), message);
    }

    public String parseMessageContent(String messageType, String message)
    {
        int contentIndex = "c2/".length() + messageType.length() + 1; // 1 for the space between type and content
        if (contentIndex >= message.length())
            return "";
        return message.substring(contentIndex);
    }

    public String[] parseMessageArguments(String messageContent)
    {
        if (messageContent.isEmpty())
            return new String[0];
        if (!messageContent.contains("; "))
            return new String[] { messageContent };
        String[] args = messageContent.split("; ");
        return args;
    }

    public ClientMessage parseMessage(String message)
    {
        String type = parseMessageType(message);
        String content = parseMessageContent(type, message);
        String[] args = parseMessageArguments(content);
        return new ClientMessage(type, content, args);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        ServerUser user = new ServerUser();
        user.conn = conn;
        connectedUsers.put(conn, user);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        if (!connectedUsers.containsKey(conn))
            return;
        ServerUser user = connectedUsers.get(conn);
        if (user.state != ServerUserState.DISCONNECTED)
        {
            handleSuddenDisconnect(user);
        }
        connectedUsers.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        assert connectedUsers.containsKey(conn) : "Failed to find user in connected users";
        ServerUser user = connectedUsers.get(conn);
        if (user.state != ServerUserState.CONNECTED && user.state != ServerUserState.CONNECTING)
        {
            conn.send(new ClientMessage("ERROR", "You have been disconnected from the server", this).toString());
            return;
        }
        if (!message.startsWith("c2/"))
        {
            conn.send("c2/ERROR Invalid format!");
            return;
        }
        
        handleMessage(user, parseMessage(message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.err.println("Failed to start server on port " + getPort() + ": " + ex.getMessage());
    }

    @Override
    public void onStart()
    {
        System.out.println("Started server on port " + getPort() + " with ID " + serverID + " and display name \"" + serverName + "\"");
    }

}
