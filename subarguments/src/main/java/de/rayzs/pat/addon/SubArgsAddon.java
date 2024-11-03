package de.rayzs.pat.addon;

import de.rayzs.pat.addon.utils.Responses;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.events.*;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class SubArgsAddon {

    public static List<String> GENERAL_LIST, BLOCKED_MESSAGE, PLAYER_NAMES;
    public static HashMap<UUID, Argument> PLAYER_COMMANDS = new HashMap<>();

    private static ConfigurationBuilder CONFIGURATION;

    public static void onLoad(ConfigurationBuilder configuration) {
        CONFIGURATION = configuration;

        updateList();
        updateMessages();
        updatePlayerNames();

        PATEventHandler.register(new TabCompletion());
        PATEventHandler.register(new ExecuteCommand());

        PATEventHandler.register(UpdateList.UPDATE_PLUGIN_EVENT);
        PATEventHandler.register(UpdateList.RECEIVE_SYNC_EVENT);
        PATEventHandler.register(UpdateList.UPDATE_PLAYER_COMMANDS_EVENT);
        PATEventHandler.register(UpdateList.SERVER_PLAYERS_CHANGE_EVENT);
    }

    public static void updatePlayerNames() {
        PLAYER_NAMES = Reflection.isProxyServer()
                ? Reflection.isVelocityServer()
                ? VelocityLoader.getPlayerNames()
                : BungeeLoader.getPlayerNames()
                : BukkitLoader.getPlayerNames();
    }

    public static ConfigurationBuilder getConfiguration() {
        return CONFIGURATION;
    }

    public static List<String> getPlayerNames() {
        return PLAYER_NAMES;
    }

    public static void updateList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).toList();

        Argument.clearArguments();
        GENERAL_LIST.forEach(Argument::buildArguments);
        SubArgsAddon.PLAYER_COMMANDS = new HashMap<>();
    }

    public static void updateMessages() {
        CONFIGURATION.reload();
        Responses.update();
    }

    public static List<String> getGroupCommands(UUID uuid) {
        List<String> commands = new ArrayList<>();
        List<Group> groups = new ArrayList<>();

        Group currentGroup;
        for (String permission : PermissionUtil.getPermissions(uuid)) {
            if(!permission.startsWith("proantitab.group.")) continue;
            currentGroup = GroupManager.getGroupByName(StringUtils.replaceFirst(permission, "proantitab.group.", ""));
            if(currentGroup != null && !groups.contains(currentGroup)) groups.add(currentGroup);
        }

        if(!groups.isEmpty()) {
            int highestPriority = groups.stream().mapToInt(Group::getPriority).filter(group -> group <= 9999).min().orElse(9999);
            groups.removeIf(group -> group.getPriority() > highestPriority);
            for (Group group : groups) commands.addAll(group.getCommands().stream().filter(command -> command.contains(" ")).toList());
        }

        return commands;
    }
}