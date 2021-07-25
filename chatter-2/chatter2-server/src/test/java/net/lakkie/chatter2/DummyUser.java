package net.lakkie.chatter2;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Stack;

import javax.net.ssl.SSLSession;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;

public class DummyUser extends ServerUser {
    
    private Stack<String> messages = new Stack<String>();

    public DummyUser()
    {
        super();
        this.conn = new WebSocket(){

            @Override
            public void close(int code, String message) { close(); }

            @Override
            public void close(int code) { close(); }

            @Override
            public void close() { }

            @Override
            public void closeConnection(int code, String message) { }

            @Override
            public void send(String text) { messages.push(text); }

            @Override
            public void send(ByteBuffer bytes) { send(new String(bytes.array())); }

            @Override
            public void send(byte[] bytes) { send(new String(bytes)); }

            @Override
            public void sendFrame(Framedata framedata) {
                messages.push(new String(framedata.getPayloadData().array()));
            }

            @Override
            public void sendFrame(Collection<Framedata> frames) {
                for (Framedata framedata : frames) {
                    messages.push(new String(framedata.getPayloadData().array()));
                }
            }

            @Override
            public void sendPing() { }

            @Override
            public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) { }

            @Override
            public boolean hasBufferedData() { return false; }

            @Override
            public InetSocketAddress getRemoteSocketAddress() { return null; }

            @Override
            public InetSocketAddress getLocalSocketAddress() { return null; }

            @Override
            public boolean isOpen() { return false; }

            @Override
            public boolean isClosing() { return false; }

            @Override
            public boolean isFlushAndClose() { return false; }

            @Override
            public boolean isClosed() { return false; }

            @Override
            public Draft getDraft() { return null; }

            @Override
            public ReadyState getReadyState() { return null; }

            @Override
            public String getResourceDescriptor() { return null; }

            @Override
            public <T> void setAttachment(T attachment){ }

            @Override
            public <T> T getAttachment() { return null; }

            @Override
            public boolean hasSSLSupport() { return false; }

            @Override
            public SSLSession getSSLSession() throws IllegalArgumentException { return null; }
            
        };
    }

    public String getLastMessage() {
        return messages.peek();
    }

    public String popLastMessage() {
        return messages.pop();
    }

    public Stack<String> getMessages() {
        return messages;
    }

}
