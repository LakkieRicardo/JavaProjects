package client.commands.impl;

import client.Client;
import client.ServerConnection;
import client.commands.ICommand;

public class CommandQuit implements ICommand {

    public void interpret(Client client, ServerConnection server, String[] args) {
        client.quit();
        System.exit(0);
    }

    public String getHelpContext() {
        return "/quit - safely disconnects and exists the application";
    }
    
    public String getCommandName() {
        return "quit";
    }

}
