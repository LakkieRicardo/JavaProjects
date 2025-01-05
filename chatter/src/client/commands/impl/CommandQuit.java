package client.commands.impl;

import client.ChatterClient;
import client.ChatterServerConnection;
import client.commands.ICommand;

public class CommandQuit implements ICommand {

    public void interpret(ChatterClient client, ChatterServerConnection server, String[] args) {
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
