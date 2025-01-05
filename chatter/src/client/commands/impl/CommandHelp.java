package client.commands.impl;

import client.ChatterClient;
import client.ChatterServerConnection;
import client.commands.CommandHelper;
import client.commands.CommandManager;
import client.commands.ICommand;

public class CommandHelp implements ICommand {

    public void interpret(ChatterClient client, ChatterServerConnection server, String[] args) {
        CommandHelper.logCommandResponse("Commands:");
        for (ICommand command : CommandManager.getCommands()) {
            CommandHelper.logCommandResponse(" " + command.getHelpContext());
        }
    }

    public String getHelpContext() {
        return "/help - displays this help context";
    }

    public String getCommandName() {
        return "help";
    }

}
