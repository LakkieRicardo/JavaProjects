package net.lakkie.chatter2;

public class ServerProperties {
    
    public static final int MIN_USERNAME_LENGTH = 1, MAX_USERNAME_LENGTH = 16;
    public static final int MAX_CONNECTIONS = 50;
    public static final String[] ILLEGAL_USERNAME_STRINGS = new String[] { ";" };

    private ServerProperties() { }

}
