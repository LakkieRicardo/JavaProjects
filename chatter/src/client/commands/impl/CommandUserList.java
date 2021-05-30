package client.commands.impl;

import client.Client;
import client.ServerConnection;
import client.commands.CommandHelper;
import client.commands.ICommand;

public class CommandUserList implements ICommand {

    public void interpret(Client client, ServerConnection server, String[] args) {
        try {
            CommandHelper.logCommandResponse("Connected users: " + client.getServer().getConnectedUsers(true));
        } catch (Exception e) {
            e.printStackTrace();
            CommandHelper.logCommandResponse("Error running command; check console");
        }
    }

    public String getHelpContext() {
        return "/listusers - displays all connected users";
    }

    public String getCommandName() {
        return "listusers";
    }
    
}
