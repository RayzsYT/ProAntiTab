package de.rayzs.pat.plugin.process.impl.server;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ServRemoveCommand extends ProCommand {

    public ServRemoveCommand() {
        super(
                "remove",
                "rem", "rm"
        );

        proxyOnly = true;
        serverCommand = true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 2)
            return false;

        String serverName = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        String fullString = String.join(" ", args);
        String command = fullString;

        if (!command.startsWith("\"")) {
            command = args[0];
        } else {

            command = command.substring(1);
            int lastIndex = command.indexOf("\"");

            if (lastIndex == -1) {
                return false;
            }

            command = command.substring(0, lastIndex);

            fullString = fullString.replace("\"" + command + "\"", ":::");
            args = fullString.split(" ");
        }

        command = StringUtils.replaceTriggers(command, "", "\\.", "'", "\"");

        if (command.isEmpty())
            return false;

        final int length = args.length;

        if (length == 1) {

            GeneralBlacklist blacklist = Storage.Blacklist.getServerBlacklist(serverName);

            boolean exist = command.contains(" ")
                    ? blacklist.getCommands().contains(command)
                    : blacklist.isListed(command);

            if (exist) {
                blacklist.remove(command).save();
                Storage.handleChange(serverName);
            }

            String message = !exist
                    ? Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SERVER_FAILED
                    : Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SERVER_SUCCESS;

            message = StringUtils.replace(message,
                    "%command%", command,
                    "%server%", serverName
            );

            sender.sendMessage(message);
            return true;
        }

        String groupName = args[1];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {

            sender.sendMessage(
                    Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST_SERVER
                            .replace("%group%", groupName)
                            .replace("%server%", serverName)
            );

            return true;
        }

        GroupBlacklist groupBlacklist = group.getOrCreateGroupBlacklist(serverName);
        boolean exist = groupBlacklist != null && groupBlacklist.getCommands().contains(command);

        if (exist) {
            group.remove(command, serverName);
            Storage.handleChange(serverName);
        }

        String message = !exist
                ? Storage.ConfigSections.Messages.GROUP.REMOVE_SERVER_FAILED
                : Storage.ConfigSections.Messages.GROUP.REMOVE_SERVER_SUCCESS;

        message = StringUtils.replace(message,
                "%group%", groupName,
                "%command%", command,
                "%server%", serverName
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return Storage.Blacklist.getBlacklists().stream().map(Map.Entry::getKey).toList();
        }

        String serverName = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        String fullString = String.join(" ", args);
        String command = fullString;

        if (args[0].startsWith("\"")) {
            command = command.length() == 1 ? "" : command.substring(1, command.length() - 1);
            int lastIndex = command.indexOf("\"");

            if (lastIndex == -1) {

                if (args.length == 1) {
                    Stream<String> stream = new ArrayList<>(Storage.Blacklist.getServerBlacklist(serverName).getCommands())
                            .stream()
                            .filter(str -> str.contains(" "));

                    if (!Reflection.isProxyServer())
                        stream = stream.map(str -> "\"" + str + "\"");

                    return stream.toList();
                }

                return null;
            }

            command = command.substring(0, lastIndex);
            args = Arrays.copyOfRange(args, command.split(" ").length - 1, args.length);
        }

        final int length = args.length;
        return length == 2 ? GroupManager.getGroupNames() : null;
    }
}

