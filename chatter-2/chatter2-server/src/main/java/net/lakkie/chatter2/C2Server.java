package net.lakkie.chatter2;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import net.lakkie.chatter2.ServerUser.ServerUserState;

public class C2Server extends WebSocketServer {

    public final Map<WebSocket, ServerUser> connectedUsers = new HashMap<WebSocket, ServerUser>();
    public final Map<UUID, ChatMessage> messageHistory = new LinkedHashMap<UUID, ChatMessage>();

    /**
     * UUID of the last message sent used for debugging purposes
     */
    private UUID lastMessageSentUUID = null;

    public final int serverID;
    public String serverName;

    public C2Server(int port, int serverID, String serverName)
    {
        super(new InetSocketAddress(port));
        this.serverID = serverID;
        this.serverName = serverName;
    }

    public void handleUpdateUsernameRequest(ServerUser user, String propertyValue)
    {
        if (propertyValue.length() <= ServerProperties.MAX_USERNAME_LENGTH)
            {
                for (String illegalString : ServerProperties.ILLEGAL_USERNAME_STRINGS)
                {
                    if (propertyValue.contains(illegalString))
                    {
                        user.conn.send(new ClientMessage("ERROR", "Username contains illegal string").toString());
                        return;
                    }
                }
                user.username = propertyValue;

                user.conn.send(new ClientMessage("ACKNOWLEDGE", "").toString());
                broadcast(new ClientMessage("UPDATE", user.username + "; username; " + propertyValue).toString());
            }
            else
                user.conn.send(new ClientMessage("ERROR", "Username is too long in length").toString());
    }

    public void handleUpdateRequest(ServerUser user, ClientMessage message)
    {
        if (message.args.length == 2)
        {
            String propertyName = message.args[0];
            String propertyValue = message.args[1];
            if (propertyName.equals("username"))
            {
                handleUpdateUsernameRequest(user, propertyValue);
            }
            else
                user.conn.send(new ClientMessage("ERROR", "Specified property name is invalid").toString());
        }
        else
            user.conn.send(new ClientMessage("ERROR", "Invalid arguments specified").toString());
    }

    public void handleActiveUsersQuery(ServerUser user, ClientMessage message)
    {
        StringBuilder userList = new StringBuilder();
        userList.append('[');
        int i = 0;
        for (ServerUser itUser : connectedUsers.values())
        {
            if (i++ == 0)
                userList.append(itUser.username);
            else
                userList.append(',').append(itUser.username);
        }
        userList.append(']');
        user.conn.send(new ClientMessage("ACKNOWLEDGE", new String(userList)).toString());
    }

    public void handleUpdateMessageQuery(ServerUser user, ClientMessage message)
    {
        if (message.args.length >= 3)
        {
            try
            {
                UUID msgUuid = UUID.fromString(message.args[1]); // Throws an exception if invalid format
                if (!messageHistory.containsKey(msgUuid)) throw new Exception();
                ChatMessage chatMessage = messageHistory.get(msgUuid);
                chatMessage.value = message.getOriginalMessage().substring(("c2/QUERY update_message; " + msgUuid.toString() + "; ").length());
                user.conn.send(new ClientMessage("ACKNOWLEDGE", "").toString());
                broadcast(new ClientMessage("MSG", chatMessage.sender.username + "; " + chatMessage.timestamp + "; " + msgUuid.toString() + "; " + chatMessage.value).toString()); // Broadcast updated message to all users
            }
            catch (Exception e)
            {
                user.conn.send(new ClientMessage("ERROR", "Invalid message UUID specified").toString());
            }
            
        }
        else
            user.conn.send(new ClientMessage("ERROR", "Invalid arguments for update_message query").toString());
    }

    /**
     * Handles a QUERY request.
     * @param user User sending the chat message. Must be in <code>CONNECTED</code> state.
     * @param message Message sent by user
     */
    public void handleQuery(ServerUser user, ClientMessage message)
    {
        if (message.args.length >= 1)
        {
            String queryType = message.args[0];
            if (queryType.equals("active_users"))
            {
                handleActiveUsersQuery(user, message);
            }
            else if (queryType.equals("update_message"))
            {
                handleUpdateMessageQuery(user, message);
            }
        }
        else
            user.conn.send(new ClientMessage("ERROR", "No query specified").toString());
    }

    /**
     * Handles a MSG request.
     * @param user The user sending the chat message. Must be in <code>CONNECTED</code> state.
     * @param timestamp User-sent timestamp of the chat message
     * @param message Contents of the chat message
     */
    public void handleChatMessage(ServerUser user, long timestamp, String message)
    {
        if (message.length() <= ServerProperties.MAX_MESSAGE_LENGTH)
        {
            ChatMessage chatMessage = new ChatMessage(user, message, timestamp);
            UUID messageID = UUID.randomUUID();
            messageHistory.put(messageID, chatMessage);
            broadcast(new ClientMessage("MSG", user.username + "; " + timestamp + "; " + messageID + "; " + message).toString());
            lastMessageSentUUID = messageID;
            user.conn.send(new ClientMessage("ACKNOWLEDGE", "").toString());
        }
        else
            user.conn.send(new ClientMessage("ERROR", "Message is above max length").toString());
    }

    /**
     * Handles a CONNECT message
     * @param user The connection attempting to request to CONNECT. Must be in <code>CONNECTING</code> state
     * @param message An object containing the client message. Request will be rejected if a valid username is not specified
     */
    public void handleUserConnection(ServerUser user, ClientMessage message)
    {
        if (message.args.length == 1)
        {
            String providedUsername = message.args[0];
            if (providedUsername.length() < ServerProperties.MIN_USERNAME_LENGTH || providedUsername.length() > ServerProperties.MAX_USERNAME_LENGTH)
            {
                user.conn.send(new ClientMessage("ERROR", "Invalid username length").toString());
                return;
            }
            for (String illegalUsernameString : ServerProperties.ILLEGAL_USERNAME_STRINGS)
            {
                if (providedUsername.contains(illegalUsernameString)) // check if contains string
                {
                    user.conn.send(new ClientMessage("ERROR", "Username contains illegal string").toString());
                    return;
                }
            }
            user.username = providedUsername;
            user.state = ServerUserState.CONNECTED;
            user.conn.send(new ClientMessage("ACKNOWLEDGE", serverName).toString());
        }
        else
            user.conn.send(new ClientMessage("ERROR", "Invalid connect command!").toString());
    }

    /**
     * Handles a PING request. User can be in any state for this
     * @param user User sending the request
     * @param message Contents of the request
     */
    public void handleUserPing(ServerUser user, ClientMessage message)
    {
        if (message.args.length == 0)
        {
            user.timeOfLastPing = System.currentTimeMillis();
            user.conn.send(new ClientMessage("PING", "" + serverID).toString());
        }
        else if (message.args.length == 1)
        {
            try
            {
                long timestamp = Long.parseLong(message.args[0]);
                long currentTime = System.currentTimeMillis();
                user.timeOfLastPing = currentTime;
                user.lastPingTime = currentTime - timestamp;
                user.conn.send(new ClientMessage("PING", currentTime + "; " + serverID).toString());
            } catch (NumberFormatException e)
            {
                user.conn.send(new ClientMessage("ERROR", "Invalid timestamp!").toString());
            }
        }
    }

    /**
     * Handles a DISCONNECT request
     * @param user User sending the request. Must be in <code>CONNECTED</code> state, unless <code>disconnectType</code> is <code>lost_connection</code>, in which case the user can be <code>null</code>.
     * @param message Contents of the message
     * @param disconnectType Type of disconnect. Can be one of the following:
     * <ul>
     *  <li><code>disconnect</code> - When a user normally disconnects</li>
     *  <li><code>lost_connection</code> - When a user suddenly disconnects</li>
     *  <li><code>ban</code> - When a user has been banned by the server and must be disconnected</li>
     * </ul>
     */
    public void handleUserDisconnect(ServerUser user, ClientMessage message, String disconnectType)
    {
        if (user != null)
        {
            user.state = ServerUserState.DISCONNECTED;
        }
        broadcast("c2/DISCONNECT " + disconnectType + "; " + user.username);
    }

    /**
     * Handles any message sent by the server. Message will be sent to appropriate methods depending on the type
     * @param user User who sent the message
     * @param message Contents of the message
     */
    public void handleMessage(ServerUser user, ClientMessage message)
    {
        if (message.type.equals("CONNECT"))
        {
            if (user.state == ServerUserState.CONNECTING)
                handleUserConnection(user, message);
            else
                user.conn.send(new ClientMessage("ERROR", "Cannot be in state \"" + user.state + "\" and send CONNECT message!").toString());
        }
        else if (message.type.equals("DISCONNECT"))
        {
            if (user.state == ServerUserState.CONNECTED)
                handleUserDisconnect(user, message, "disconnect");
            else if (user.state == ServerUserState.CONNECTING)
                user.state = ServerUserState.DISCONNECTED; // If never connected, do not broadcast to all users
            else
                user.conn.send(new ClientMessage("ERROR", "Cannot be in state \"" + user.state + "\" and send DISCONNECT message!").toString());
        }
        else if (message.type.equals("PING"))
        {
            handleUserPing(user, message);
        }
        else if (message.type.equals("QUERY"))
        {
            handleQuery(user, message);
        }
        else if (message.type.equals("MSG"))
        {
            if (user.state == ServerUserState.CONNECTED)
            {
                String originalMessage = message.getOriginalMessage();
                String timestamp = originalMessage.substring("c2/MSG ".length(), originalMessage.indexOf("; "));
                try
                {
                    long timestampL = Long.parseLong(timestamp);
                    String chatMessageValue = originalMessage.substring(originalMessage.indexOf("; ") + "; ".length());
                    handleChatMessage(user, timestampL, chatMessageValue);
                }
                catch (NumberFormatException e)
                {
                    user.conn.send(new ClientMessage("ERROR", "Invalid timestamp").toString());
                }
            }
            else
                user.conn.send(new ClientMessage("ERROR", "Cannot be in state \"" + user.state + "\" and send MSG message!").toString());
        }
        else if (message.type.equals("UPDATE"))
        {
            if (user.state == ServerUserState.CONNECTED)
            {
                handleUpdateRequest(user, message);
            }
            else
                user.conn.send(new ClientMessage("ERROR", "Cannot be in state \"" + user.state + "\" and send UPDATE message!").toString());
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        ServerUser user = new ServerUser();
        user.conn = conn;
        user.state = ServerUserState.CONNECTING;
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
            handleUserDisconnect(user, null, "lost_connection");
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
            conn.send(new ClientMessage("ERROR", "You have been disconnected from the server").toString());
            return;
        }
        if (!message.startsWith("c2/"))
        {
            conn.send("c2/ERROR Invalid format!");
            return;
        }
        
        handleMessage(user, ClientMessage.parseMessage(message));
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

    public UUID getLastMessageSentUUID() {
        return lastMessageSentUUID;
    }

}
