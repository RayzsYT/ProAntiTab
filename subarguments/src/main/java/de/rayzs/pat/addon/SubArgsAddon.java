package de.rayzs.pat.addon;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.addon.events.*;
import java.util.*;

public class SubArgsAddon {

    public static List<String> GENERAL_LIST;
    public static List<String> BLOCKED_MESSAGE;
    private static ConfigurationBuilder CONFIGURATION = Configurator.get("config", "./plugins/ProAntiTab/addons/SubArguments");

    public static void onLoad() {
        updateList();
        updateMessages();

        PATEventHandler.register(new TabCompletion());
        PATEventHandler.register(Reflection.isProxyServer() ? new BungeeExecuteCommand() : new BukkitExecuteCommand());

        PATEventHandler.register(UpdateList.UPDATE_PLUGIN_EVENT);
        PATEventHandler.register(UpdateList.RECEIVE_SYNC_EVENT);
    }

    public static void updateList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).toList();
    }

    public static void updateMessages() {
        CONFIGURATION.reload();
        BLOCKED_MESSAGE = (List<String>) CONFIGURATION.getOrSet("blocked-message", new ArrayList<>(List.of("Subcommand %sub% for %command% is blocked", "Sry mate!")));
    }
}