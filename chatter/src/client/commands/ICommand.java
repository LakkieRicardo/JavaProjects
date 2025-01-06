package client.commands;

import client.ChatterClient;
import client.ChatterServerConnection;

public interface ICommand {

    void interpret(ChatterClient client, ChatterServerConnection server, String[] args);

    String getHelpContext();

    String getCommandName();

}