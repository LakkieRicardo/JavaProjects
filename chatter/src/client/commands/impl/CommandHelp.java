package client.commands.impl;

import client.Client;
import client.ServerConnection;
import client.commands.CommandHelper;
import client.commands.CommandManager;
import client.commands.ICommand;

public class CommandHelp implements ICommand {

    public void interpret(Client client, ServerConnection server, String[] args) {
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
