package client.commands;

import org.fusesource.jansi.Ansi;

import client.ClientRender;

public class CommandHelper {
    
    private CommandHelper() { }

    public static void logCommandResponse(String message) {
        ClientRender.showMessage(Ansi.ansi().fgBlue() + "[Client] " + Ansi.ansi().fgBlack() + message);
    }

}
