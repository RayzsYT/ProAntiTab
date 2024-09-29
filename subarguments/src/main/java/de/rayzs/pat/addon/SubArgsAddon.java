package de.rayzs.pat.addon;

import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.events.*;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.util.*;

public class SubArgsAddon {

    public static List<String> GENERAL_LIST, BLOCKED_MESSAGE, STARTING_LIST;
    public static HashMap<UUID, List<String>> PLAYER_COMMANDS = new HashMap<>();

    private static ConfigurationBuilder CONFIGURATION;

    public static void onLoad(ConfigurationBuilder configuration) {
        CONFIGURATION = configuration;

        updateList();
        updateMessages();

        PATEventHandler.register(new TabCompletion());
        PATEventHandler.register(new ExecuteCommand());

        PATEventHandler.register(UpdateList.UPDATE_PLUGIN_EVENT);
        PATEventHandler.register(UpdateList.RECEIVE_SYNC_EVENT);
    }

    public static void updateList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).toList();
        STARTING_LIST = new ArrayList<>();

        Argument.clearArguments();
        GENERAL_LIST.forEach(Argument::buildArguments);
    }

    public static void updateMessages() {
        CONFIGURATION.reload();

        ArrayList<String> MESSAGE = new ArrayList<>();
        MESSAGE.add("&8[&4ProAntiTab&8] &cThis argument is not allowed!");
        BLOCKED_MESSAGE = (List<String>) CONFIGURATION.getOrSet("blocked-message", MESSAGE);
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