package util;

public class ServerDataCodes {

    // Message types
    public static final String REQUEST_CODE = "RQ:", RESPONSE_CODE = "RP:", UPDATE_CODE = "UP:", PING_CODE = "ping", CLIENT_PING_CODE = "cping";

    // Data type codes
    public static final String USERNAME_CODE = "nn";

    public static final String USER_LIST_CODE = "ul";

    public static final String MESSAGE_CODE = "ms";

    public static final String NEW_USER_CODE = "nu";

    public static final String DISCONNECT_CODE = "dc";

    public static final String BROADCAST_CODE = "bc";

    public static final String SERVER_SHUTDOWN_CODE = "sd";

    public static final String BANNED_CODE = "bn";

    // Response code data types

    public static final String RESPONSE_OK_CODE = "ok";

    public static final String RESPONSE_ERR_CODE = "er";

    private ServerDataCodes() { }

}
