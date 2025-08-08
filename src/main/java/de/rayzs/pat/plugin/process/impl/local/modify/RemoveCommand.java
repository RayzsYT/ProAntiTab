package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;
import java.util.stream.Stream;

public class RemoveCommand extends ProCommand {

    public RemoveCommand() {
        super(
                "remove",
                "rem", "rm"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length == 0)
            return false;

        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        if (backend) {
            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
            return true;
        }

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
            boolean exist = Storage.Blacklist.getBlacklist().getCommands().contains(command);

            if (exist) {
                Storage.Blacklist.getBlacklist().remove(command).save();
                Storage.handleChange();
            }

            String message = !exist ? Storage.ConfigSections.Messages.BLACKLIST.REMOVE_FAILED : Storage.ConfigSections.Messages.BLACKLIST.REMOVE_SUCCESS;
            message = StringUtils.replace(message, "%command%", command);

            sender.sendMessage(message);
            return true;
        }

        String groupName = args[1];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", groupName));
            return true;
        }

        GroupBlacklist blacklist = group.getGeneralGroupBlacklist();
        boolean exist = blacklist.getCommands().contains(command);

        if (exist) {
            group.remove(command);
            Storage.handleChange();
        }

        String message = !exist ? Storage.ConfigSections.Messages.GROUP.REMOVE_FAILED : Storage.ConfigSections.Messages.GROUP.REMOVE_SUCCESS;
        message = StringUtils.replace(message, "%group%", groupName, "%command%", command);

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0 || args.length == 1 && !args[0].startsWith("\"")) {
            return new ArrayList<>(Storage.Blacklist.getBlacklist().getCommands()).stream().filter(str -> !str.contains(" ")).toList();
        }

        String fullString = String.join(" ", args);
        String command = fullString;

        if (args[0].startsWith("\"")) {
            command = command.length() == 1 ? "" : command.substring(1, command.length() - 1);
            int lastIndex = command.indexOf("\"");

            if (lastIndex == -1) {

                if (args.length == 1) {
                    Stream<String> stream = new ArrayList<>(Storage.Blacklist.getBlacklist().getCommands())
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
