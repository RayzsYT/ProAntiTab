package de.rayzs.pat.addon;

import de.rayzs.pat.addon.events.ExecuteCommand;
import de.rayzs.pat.addon.events.TabCompletion;
import de.rayzs.pat.addon.events.UpdateList;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.configuration.impl.BukkitConfigurationBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class SubArgsAddon extends JavaPlugin {

    public static List<String> GENERAL_LIST;
    public static List<String> BLOCKED_MESSAGE;

    private static ConfigurationBuilder CONFIGURATION = Configurator.get("config", "./plugins/ProAntiTab/addons/SubArguments");

    @Override
    public void onEnable() {
        updateList();
        updateMessages();

        PATEventHandler.register(new TabCompletion());
        PATEventHandler.register(new ExecuteCommand());

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