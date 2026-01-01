package de.rayzs.pat.plugin.subarguments;

import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.plugin.subarguments.events.ExecuteCommand;
import de.rayzs.pat.plugin.subarguments.events.TabCompletion;
import de.rayzs.pat.plugin.subarguments.events.UpdateList;
import de.rayzs.pat.utils.node.BukkitCommandNodeHelper;
import de.rayzs.pat.utils.node.ProxyCommandNodeHelper;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import de.rayzs.pat.utils.subargs.*;
import java.util.stream.Collectors;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class SubArguments {

    public static List<String> GENERAL_LIST, BLOCKED_MESSAGE, PLAYER_NAMES, ONLINE_PLAYERS;
    public static HashMap<UUID, Arguments> PLAYER_COMMANDS = new HashMap<>();

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
        PLAYER_NAMES = Storage.getLoader().getPlayerNames();
        ONLINE_PLAYERS = Storage.getLoader().getOnlinePlayerNames();
    }

    public static List<String> getPlayerNames() {
        return PLAYER_NAMES;
    }

    public static List<String> getOnlinePlayerNames() {
        return ONLINE_PLAYERS;
    }

    public static void updateList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).collect(Collectors.toList());

        Arguments.ARGUMENTS.clearArguments();

        GENERAL_LIST.forEach(command -> Arguments.ARGUMENTS.buildArgumentStacks(command));
        SubArguments.PLAYER_COMMANDS = new HashMap<>();
    }

    public static void updateMessages() {
        Storage.Files.CUSTOM_RESPONSES.reload();
        ResponseHandler.update();
    }

    public static void handleCommandNode(UUID uuid, ProxyCommandNodeHelper helper) {
        if (!Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return;

        Arguments arguments = PLAYER_COMMANDS.get(uuid);
        if (arguments == null) {
            return;
        }

        ArgumentSource source = arguments.TAB_ARGUMENTS;
        if (source == null) {
            return;
        }

        List<String> inputs = source.getAllInputs();
        List<String> allEntries = inputs.stream().map(StringUtils::getFirstArg).toList();

        helper.removeIf(allEntries::contains);

        for (String input : inputs) {
            final boolean negated = Storage.Blacklist.BlockTypeFetcher.isNegated(input);

            if (!negated) {
                helper.add(input, true);
                continue;
            }

            final Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(input);
            if (type != Storage.Blacklist.BlockType.BOTH && type != Storage.Blacklist.BlockType.TAB) {
                continue;
            }

            input = Storage.Blacklist.BlockTypeFetcher.cleanse(input);
            helper.removeSubArguments(input);
        }
    }

    public static void handleCommandNode(UUID uuid, BukkitCommandNodeHelper helper) throws Exception {
        if (!Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return;

        Arguments arguments = PLAYER_COMMANDS.get(uuid);
        if (arguments == null) {
            return;
        }

        ArgumentSource source = arguments.TAB_ARGUMENTS;
        if (source == null) {
            return;
        }

        List<String> inputs = source.getAllInputs();
        List<String> argumentsList = new ArrayList<>();

        for (String input : inputs) {
            final Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(input);
            if (type == Storage.Blacklist.BlockType.CHAT) {
                continue;
            }

            final boolean negated = Storage.Blacklist.BlockTypeFetcher.isNegated(input);
            input = Storage.Blacklist.BlockTypeFetcher.cleanse(input);

            if (!negated) {
                argumentsList.add(input);
                continue;
            }

            helper.removeSubArguments(input);
        }


        // Now use the argumentsList to have it as a sorta whitelist on what sub-arguments are allowed.
        // For that, take the base command first and check for the child if it exists.
        // If yes, you that child and go through the children recursively.
        //
        // If the amounts of spaces and the child-indexes is correct, then allow it.
        // Otherwise, remove the other children.
        //
        // Idea for the recursive function:
        // spareRecursively(List<String>, BukkitCommandNodeHelper, int index)
        System.out.println("-> " + argumentsList);
        if (argumentsList.isEmpty()) {
            return;
        }

        helper.spareRecursively(argumentsList);
    }

    public static List<String> getServerCommands(UUID uuid) {
        return getServerCommands(Storage.getLoader().getPlayerServerName(uuid));
    }

    public static List<String> getServerCommands(String serverName) {
        List<String> commands = new ArrayList<>(Storage.Blacklist.getBlacklist().getCommands());
        if (!Reflection.isProxyServer())
            return commands;

        if (serverName == null)
            return commands;

        List<GeneralBlacklist> blacklists = Storage.Blacklist.getServerBlacklists(serverName);
        for (GeneralBlacklist blacklist : blacklists)
            commands.addAll(blacklist.getCommands());

        return commands;
    }

    public static List<String> getGroupCommands(UUID uuid) {
        return getGroupCommands(uuid, null);
    }

    public static List<String> getGroupCommands(UUID uuid, String serverName) {
        List<String> commands = new ArrayList<>();
        List<Group> groups;

        if (Storage.getPermissionPlugin() == PermissionPlugin.NONE) {
            CommandSender sender = CommandSenderHandler.from(uuid);
            groups = GroupManager.getPlayerGroups(sender);
        } else {
            groups = GroupManager.getPlayerGroups(uuid);
        }

        if (serverName == null) {
            groups.forEach(group -> commands.addAll(group.getCommands()));
            return commands;
        }

        groups.forEach(group -> {

            commands.addAll(group.getCommands());

            List<String> associatedServerNames = group.getBlacklistServerNames(serverName);

            associatedServerNames.forEach(s -> {
                commands.addAll(group.getCommands(s));
            });

        });
        return commands;
    }

    public static String replacePlaceholders(String input) {
        String[] split = input.split(" ");

        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            boolean number = NumberUtils.isDigit(split[i]),
                    online = Storage.getLoader().isPlayerOnline(s),
                    general = Storage.getLoader().doesPlayerExist(s);

            if (number) {
                split[i] = "%numbers%";
                continue;
            }

            if (online && general) {
                split[i] = "%both_players%";
                continue;
            }

            if (online) {
                split[i] = "%online_players%";
                continue;
            }

            if (general)
                split[i] = "%players%";
        }

        return String.join(" ", split);
    }
}