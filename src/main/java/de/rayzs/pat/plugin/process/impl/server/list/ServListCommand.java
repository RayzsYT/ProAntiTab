package de.rayzs.pat.plugin.process.impl.server.list;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServListCommand extends ProCommand {

    public ServListCommand() {
        super(
                "list",
                "ls"
        );

        proxyOnly = true;
        serverCommand = true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 1)
            return false;

        String serverName = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        if (args.length == 0) {

            GeneralBlacklist blacklist = Storage.Blacklist.getServerBlacklist(serverName);

            String commandsListMessage = StringUtils.getStringList(
                    blacklist.getCommands(),
                    Storage.ConfigSections.Messages.BLACKLIST.LIST_SERVER_SPLITTER
            );

            String message = Storage.ConfigSections.Messages.BLACKLIST.LIST_SERVER_MESSAGE;
            message = StringUtils.replace(message,
                    "%size%", String.valueOf(blacklist.getCommands().size()),
                    "%commands%", commandsListMessage,
                    "%server%", serverName
            );

            sender.sendMessage(message);
            return true;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST_SERVER.replace("%group%", groupName));
            return true;
        }

        String commandsListMessage = StringUtils.getStringList(
                group.getCommands(serverName),
                Storage.ConfigSections.Messages.GROUP.LIST_SERVER_SPLITTER
        );

        String message = Storage.ConfigSections.Messages.GROUP.LIST_SERVER_MESSAGE;
        message = StringUtils.replace(message,
                "%group%", groupName,
                "%size%", String.valueOf(group.getCommands(serverName).size()),
                "%commands%", commandsListMessage,
                "%server%", serverName
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        final int length = args.length;
        return length < 2 ? Storage.Blacklist.getBlacklists().stream().map(Map.Entry::getKey).toList() :
                length < 3 ? GroupManager.getGroupNames() : null;
    }
}