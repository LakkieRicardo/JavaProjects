package client.commands;

import client.Client;
import client.ServerConnection;

public interface ICommand {

    void interpret(Client client, ServerConnection server, String[] args);

    String getHelpContext();

    String getCommandName();

}