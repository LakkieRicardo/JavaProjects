package client.commands;

import java.util.*;

import org.fusesource.jansi.Ansi;

import client.ChatterClient;
import client.ChatterClientUI;
import client.commands.impl.*;

public class CommandManager {
    
    private static Map<String, ICommand> commandMap;
    private CommandManager() { }

    public static void initCommand(ICommand command) {
        commandMap.put(command.getCommandName(), command);
    }

    public static void initCommands() {
        commandMap = new HashMap<String, ICommand>();
        initCommand(new CommandQuit());
        initCommand(new CommandHelp());
        initCommand(new CommandUserList());
        initCommand(new CommandPing());
        initCommand(new CommandUsername());
    }

    public static void handleCommand(String input, ChatterClient client) {
        if (!input.startsWith("/")) {
            return;
        }
        input = input.substring(1);
        if (input.contains(" ")) {
            String[] allArgs = input.split(" ");
            String[] args = new String[allArgs.length - 1];
            for (int i = 1; i < allArgs.length; i++) {
                args[i - 1] = allArgs[i];
            }
            if (!commandMap.containsKey(allArgs[0])) {
                logInvalidCommand();
                return;
            }
            System.out.printf("Running command %s with arguments %s...\n", allArgs[0], Arrays.toString(args));
            commandMap.get(allArgs[0]).interpret(client, client.getServer(), args);
        } else {
            if (!commandMap.containsKey(input)) {
                logInvalidCommand();
                return;
            }
            System.out.printf("Running command %s with arguments []...\n", input);
            commandMap.get(input).interpret(client, client.getServer(), new String[0]);
        }
    }

    private static void logInvalidCommand() {
        ChatterClientUI.showMessage(Ansi.ansi().fgRed().a("Invalid command!").fgBlack().toString());
    }

    public static Collection<ICommand> getCommands() {
        return commandMap.values();
    }

}
