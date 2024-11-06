package de.rayzs.pat.plugin.modules.subargs;

import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.modules.subargs.events.*;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.subargs.*;
import java.util.stream.Collectors;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class SubArgsModule {

    public static List<String> GENERAL_LIST, BLOCKED_MESSAGE, PLAYER_NAMES;
    public static HashMap<UUID, Argument> PLAYER_COMMANDS = new HashMap<>();

    public static void initialize() {
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

    public static List<String> getPlayerNames() {
        return PLAYER_NAMES;
    }

    public static void updateList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).collect(Collectors.toList());

        Argument.clearArguments();
        GENERAL_LIST.forEach(Argument::buildArguments);
        SubArgsModule.PLAYER_COMMANDS = new HashMap<>();
    }

    public static void updateMessages() {
        Storage.Files.CUSTOM_RESPONSES.reload();
        Responses.update();
    }

    public static List<String> getServerCommands(UUID uuid) {
        List<String> commands = new ArrayList<>();
        if(!Reflection.isProxyServer()) return commands;

        String serverName = Reflection.isVelocityServer() ? VelocityLoader.getServerNameByPlayerUUID(uuid) : BungeeLoader.getServerNameByPlayerUUID(uuid);
        if(serverName == null) return commands;

        List<GeneralBlacklist> blacklists = Storage.Blacklist.getBlacklists(serverName);
        for (GeneralBlacklist blacklist : blacklists)
            commands.addAll(blacklist.getCommands());

        return commands;
    }

    public static List<String> getGroupCommands(UUID uuid) {
        List<String> commands = new ArrayList<>();
        List<Group> groups = new ArrayList<>();

        Group currentGroup;
        for (String permission : Objects.requireNonNull(PermissionUtil.getPermissions(uuid))) {
            if(!permission.startsWith("proantitab.group.")) continue;
            currentGroup = GroupManager.getGroupByName(StringUtils.replaceFirst(permission, "proantitab.group.", ""));
            if(currentGroup != null && !groups.contains(currentGroup)) groups.add(currentGroup);
        }

        if(!groups.isEmpty()) {
            int highestPriority = groups.stream().mapToInt(Group::getPriority).filter(group -> group <= 9999).min().orElse(9999);
            groups.removeIf(group -> group.getPriority() > highestPriority);
            for (Group group : groups) commands.addAll(group.getCommands().stream().filter(command -> command.contains(" ")).collect(Collectors.toList()));
        }

        return commands;
    }
}