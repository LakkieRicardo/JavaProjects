package net.lakkie.chatter2;

public class App 
{

    public static C2Server server;

    public static void CreateServer(int port)
    {
        if (server != null)
            throw new IllegalStateException("Tried to create server while already running server");
        server = new C2Server(port, 0, "TestServer"); // TODO add server name and ID options
        server.start();
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("No port is specified");
            System.exit(1);
        }
        try
        {
            CreateServer(Integer.parseInt(args[0]));
        } catch (NumberFormatException e)
        {
            System.err.println("Invalid port specified");
            System.exit(2);
        }
    }
}
