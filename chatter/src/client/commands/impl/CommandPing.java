package client.commands.impl;

import client.ChatterClient;
import client.ChatterServerConnection;
import client.commands.CommandHelper;
import client.commands.ICommand;

public class CommandPing implements ICommand {

    public void interpret(ChatterClient client, ChatterServerConnection server, String[] args) {
        CommandHelper.logCommandResponse("Ping: " + server.lastPing + "ms");
    }

    public String getHelpContext() {
        return "/ping - displays ping time to server";
    }

    public String getCommandName() {
        return "ping";
    }
    
}
