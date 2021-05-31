package client.commands.impl;

import java.io.IOException;

import client.Client;
import client.ServerConnection;
import client.commands.CommandHelper;
import client.commands.ICommand;

public class CommandUsername implements ICommand {

    public void interpret(Client client, ServerConnection server, String[] args) {
        if (args.length != 1) {
            CommandHelper.logCommandResponse("Incorrect usage, see /help");
            return;
        }
        if (!args[0].matches("^[a-zA-Z0-9]{1,16}$")) {
            CommandHelper.logCommandResponse("Name does not follow format(alphanumberic, 1-16 characters in length)");
            return;
        }
        try {
            server.updateUsername(args[0]);
            CommandHelper.logCommandResponse("Changing username to " + args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            CommandHelper.logCommandResponse("Failed to change username to " + args[0] + ", see console for more details");
        }
    }

    public String getHelpContext() {
        return "/rename <username> - updates your username";
    }

    public String getCommandName() {
        return "rename";
    }

}
