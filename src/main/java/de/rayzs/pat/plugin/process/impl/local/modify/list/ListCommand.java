package de.rayzs.pat.plugin.process.impl.local.modify.list;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.*;
import java.util.List;

public class ListCommand extends ProCommand {

    public ListCommand() {
        super(
                "list",
                "ls"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            String commandsListMessage = StringUtils.getStringList(
                    Storage.Blacklist.getBlacklist().getCommands(),
                    Storage.ConfigSections.Messages.BLACKLIST.LIST_SPLITTER
            );

            String message = Storage.ConfigSections.Messages.BLACKLIST.LIST_MESSAGE;
            message = StringUtils.replace(message,
                    "%size%", String.valueOf(Storage.Blacklist.getBlacklist().getCommands().size()),
                    "%commands%", commandsListMessage
            );

            sender.sendMessage(message);
            return true;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", groupName));
            return true;
        }

        String commandsListMessage = StringUtils.getStringList(
                group.getCommands(),
                Storage.ConfigSections.Messages.GROUP.LIST_SPLITTER
        );

        String message = Storage.ConfigSections.Messages.GROUP.LIST_MESSAGE;
        message = StringUtils.replace(message,
                "%group%", groupName,
                "%size%", String.valueOf(Storage.Blacklist.getBlacklist().getCommands().size()),
                "%commands%", commandsListMessage
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }
}
