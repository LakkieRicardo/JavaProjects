package client.commands.impl;

import client.Client;
import client.ServerConnection;
import client.commands.CommandHelper;
import client.commands.ICommand;

public class CommandPing implements ICommand {

    public void interpret(Client client, ServerConnection server, String[] args) {
        CommandHelper.logCommandResponse("Ping: " + server.lastPing + "ms");
    }

    public String getHelpContext() {
        return "/ping - displays ping time to server";
    }

    public String getCommandName() {
        return "ping";
    }
    
}
