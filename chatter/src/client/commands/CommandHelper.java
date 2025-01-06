package client.commands;

import org.fusesource.jansi.Ansi;

import client.ChatterClientUI;

public class CommandHelper {
    
    private CommandHelper() { }

    public static void logCommandResponse(String message) {
        ChatterClientUI.showMessage(Ansi.ansi().fgBlue() + "[Client] " + Ansi.ansi().fgBlack() + message);
    }

}
