package net.lakkie.chatter2;

import org.java_websocket.WebSocket;

public class ServerUser {
    
    public String username = null;
    public ServerUserState state = ServerUserState.INVALID;
    public WebSocket conn;
    public long timeOfLastPing, lastPingTime = -1L;

    public ServerUser(String username)
    {
        this.username = username;
        this.state = ServerUserState.CONNECTED;
        timeOfLastPing = System.currentTimeMillis();
    }

    public ServerUser()
    {
        timeOfLastPing = System.currentTimeMillis();
    }

    public static enum ServerUserState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        INVALID
    }

}
