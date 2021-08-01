package net.lakkie.chatter2;

public class App 
{

    public static C2Server server;

    public static void CreateServer(int port)
    {
        if (server != null)
            throw new IllegalStateException("Tried to create server while already running server");
        server = new C2Server(port, 0, "TestServer");
        server.start();
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Defaulting to port 5001...");
            CreateServer(5001);
        }
        else
        {
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
}
