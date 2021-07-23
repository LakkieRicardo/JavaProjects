package net.lakkie.chatter2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
        String messageType = server.parseMessageType(message);
        String messageContent = server.parseMessageContent(messageType, message);
        String[] messageArgs = server.parseMessageArguments(messageContent);
        assertEquals("QUERY", messageType);
        assertEquals("ping; true", messageContent);
        assertArrayEquals(new String[] { "ping", "true" }, messageArgs);
    }

    @Test
    public void testUserConnect()
    {
        createDummy();
        server.handleUserConnection(dummy, new ClientMessage("CONNECT", "Dummy", server));
        assertEquals("c2/ACKNOWLEDGE " + server.serverName, dummy.getLastMessage());
    }

    @Test
    public void testUserDisconnect()
    {
        createDummy();
        server.handleUserDisconnect(dummy, new ClientMessage("DISCONNECT", "", server), "disconnect");
        assertEquals("c2/ACKNOWLEDGE", dummy.getLastMessage());
    }

    @Test
    public void testUserPing()
    {
        createDummy();
        server.handleUserPing(dummy, new ClientMessage("PING", "", server));
        assertEquals("c2/PING " + server.serverID, dummy.getLastMessage());
    }

}
