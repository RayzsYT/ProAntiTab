package de.rayzs.pat.plugin.system.subargument;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.subargument.handler.impl.CommandNodeHandler;
import de.rayzs.pat.plugin.system.subargument.handler.impl.ExecuteHandler;
import de.rayzs.pat.plugin.system.subargument.handler.impl.TabCompleteHandler;
import de.rayzs.pat.plugin.system.subargument.handler.impl.UpdateArgumentsHandler;
import de.rayzs.pat.utils.NumberUtils;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.plugin.system.subargument.argument.Arguments;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SubArgument {

    private static SubArgument instance;

    public static SubArgument get() {
        return instance;
    }

    public static void initialize() {
        if (instance != null) {
            Logger.warning("SubArguments already initialized!");
            return;
        }

        instance = new SubArgument();
    }


    private final HashMap<UUID, Arguments> cachedPlayerArgument = new HashMap<>();
    private List<String> cachedPlayerNames, cachedOnlinePlayers;

    private final CommandNodeHandler commandNodeHandler;
    private final ExecuteHandler executeHandler;
    private final TabCompleteHandler tabCompleteHandler;
    private final UpdateArgumentsHandler updateArgumentsHandler;

    private SubArgument() {
        this.commandNodeHandler = new CommandNodeHandler(this);
        this.executeHandler = new ExecuteHandler(this);
        this.tabCompleteHandler = new TabCompleteHandler(this);
        this.updateArgumentsHandler = new UpdateArgumentsHandler(this);


        updateDefaultArguments();
        updateCachedPlayerNames();
    }


    public CommandNodeHandler getCommandNodeHandler() {
        return commandNodeHandler;
    }

    public ExecuteHandler getExecuteHandler() {
        return executeHandler;
    }

    public TabCompleteHandler getTabCompleteHandler() {
        return tabCompleteHandler;
    }

    public UpdateArgumentsHandler getUpdateArgumentsHandler() {
        return updateArgumentsHandler;
    }

    public Arguments getPlayerArgument(CommandSender sender) {
        return cachedPlayerArgument.getOrDefault(sender.getUniqueId(), Arguments.get());
    }


    // Updates the cached player names.
    // Those are cached for the replacePlaceholders method so
    // it doesn't fetch all player names everytime it's called.
    public void updateCachedPlayerNames() {
        cachedPlayerNames = Storage.getLoader().getPlayerNames();
        cachedOnlinePlayers = Storage.getLoader().getOnlinePlayerNames();
    }

    public void updateDefaultArguments() {
        Arguments.get().clearArguments();

        Storage.Blacklist.getBlacklist().getCommands().stream()
                .filter(command -> command.contains(" "))
                .forEach(command -> {
                    Arguments.get().buildArgumentStacks(command);
                });

        cachedPlayerArgument.clear();
    }

    // Replaces values like 123 with more generic
    // placeholders used inside the storage.yml.
    public String replaceValuesWithPlaceholders(String input) {
        final String[] split = input.split(" ");

        for (int i = 0; i < split.length; i++) {
            final String s = split[i];

            final boolean isNumber = NumberUtils.isDigit(s);
            final boolean isOnline = Storage.getLoader().isPlayerOnline(s);
            final boolean doesExist = Storage.getLoader().doesPlayerExist(s);

            if (isNumber) {
                split[i] = "%numbers%";
                continue;
            }

            if (isOnline && doesExist) {
                split[i] = "%both_players%";
                continue;
            }

            if (isOnline) {
                split[i] = "%online_players%";
                continue;
            }

            if (doesExist)
                split[i] = "%players%";
        }

        return String.join(" ", split);
    }

    public List<String> getCachedOnlinePlayerNames() {
        return cachedOnlinePlayers;
    }

    public List<String> getCachedPlayerNames() {
        return cachedPlayerNames;
    }
}
