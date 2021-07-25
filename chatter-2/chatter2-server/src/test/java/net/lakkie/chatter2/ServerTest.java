package net.lakkie.chatter2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        dummy.username = "Dummy";
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
            fail();
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

    @Test
    public void testQueryConnectedUsers()
    {
        createDummy();
        server.handleMessage(dummy, new ClientMessage("QUERY", "active_users"));
        assertEquals("c2/ACKNOWLEDGE [" + dummy.username + "]", dummy.getLastMessage());
    }

    @Test
    public void testQueryUpdateMessage()
    {
        createDummy();
        dummy.state = ServerUserState.CONNECTED;
        long timestamp = System.currentTimeMillis();
        // Send message
        server.handleMessage(dummy, new ClientMessage("MSG", timestamp + "; Test message"));
        assertNotNull("Expected the server to provide a last message UUID", server.getLastMessageSentUUID());

        // Update message
        ClientMessage newMessageQuery = new ClientMessage("QUERY", "update_message; " + server.getLastMessageSentUUID() + "; New message value");
        server.handleMessage(dummy, newMessageQuery);
        assertEquals("c2/ACKNOWLEDGE", dummy.getMessages().pop()); // Acknowledge for initial message sent
        assertEquals("c2/ACKNOWLEDGE", dummy.getMessages().pop()); // Acknowledge for message update
    }

}
