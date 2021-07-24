package net.lakkie.chatter2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.lakkie.chatter2.ServerUser.ServerUserState;

public class ServerTest {
    
    public static final String TEST_SERVER_NAME = "TestServer";
    public static final int TEST_SERVER_ID = 0, TEST_SERVER_PORT = 5000;

    private C2Server server;
    private DummyUser dummy;

    private void createServer()
    {
        if (server != null) return;
        server = new C2Server(TEST_SERVER_PORT, TEST_SERVER_ID, TEST_SERVER_NAME);
    }

    private void createDummy()
    {
        if (dummy != null) return;
        createServer();
        dummy = new DummyUser();
        server.connectedUsers.put(dummy.conn, dummy);
    }

    @Test
    public void testServerStart()
    {
        try
        {
            if (server != null) server.stop();
            server = new C2Server(TEST_SERVER_PORT, TEST_SERVER_ID, TEST_SERVER_NAME);
        } catch (Exception e)
        {
            assertTrue(false);
        }
    }

    @Test
    public void testMessageParsing()
    {
        createServer();
        String message = "c2/QUERY ping; true";
        String messageType = ClientMessage.parseMessageType(message);
        String messageContent = ClientMessage.parseMessageContent(messageType, message);
        String[] messageArgs = ClientMessage.parseMessageArguments(messageContent);
        assertEquals("QUERY", messageType);
        assertEquals("ping; true", messageContent);
        assertArrayEquals(new String[] { "ping", "true" }, messageArgs);
    }

    @Test
    public void testUserConnect()
    {
        createDummy();
        dummy.state = ServerUserState.CONNECTING;
        server.handleMessage(dummy, new ClientMessage("CONNECT", "Dummy"));
        assertEquals("c2/ACKNOWLEDGE " + server.serverName, dummy.getLastMessage());
    }

    @Test
    public void testUserDisconnect()
    {
        createDummy();
        dummy.state = ServerUserState.CONNECTED;
        server.handleMessage(dummy, new ClientMessage("DISCONNECT", ""));
        assertEquals(ServerUserState.DISCONNECTED, dummy.state);
    }

    @Test
    public void testUserPing()
    {
        createDummy();
        server.handleMessage(dummy, new ClientMessage("PING", ""));
        assertEquals("c2/PING " + server.serverID, dummy.getLastMessage());
    }

}
