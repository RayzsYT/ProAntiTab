package de.rayzs.pat.plugin.process.impl.local.modify.list;

import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import java.util.List;

public class ListGroupsCommand extends ProCommand {

    public ListGroupsCommand() {
        super(
                "listgroups",
                "lg"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
         String groupsListMessage = StringUtils.getStringList(
                    GroupManager.getGroupNames(),
                    Storage.ConfigSections.Messages.GROUP.LIST_SPLITTER
            );

            String message = Storage.ConfigSections.Messages.GROUP.LIST_GROUP_MESSAGE;
            message = StringUtils.replace(message,
                    "%size%", String.valueOf(Storage.Blacklist.getBlacklist().getCommands().size()),
                    "%groups%", groupsListMessage
            );

            sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
