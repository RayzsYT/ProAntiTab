package de.rayzs.pat.plugin.process.impl.local.modify.list;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;

public class ListPrioritiesCommand extends ProCommand {

    public ListPrioritiesCommand() {
        super(
                "listpriorities",
                "lp"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        String groupsPriorityListMessage = buildGroupPriorityMessage(
                GroupManager.getGroups(),
                Storage.ConfigSections.Messages.GROUP.LIST_PRIORITY_SPLITTER,
                Storage.ConfigSections.Messages.GROUP.LIST_PRIORITY_GROUPS
        );

        String message = StringUtils.replace(groupsPriorityListMessage,
                "%size%", String.valueOf(GroupManager.getGroups().size()),
                "%groups%", groupsPriorityListMessage
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }

    private String buildGroupPriorityMessage(List<Group> list, String splitter, String format) {
        StringBuilder builder = new StringBuilder();
        boolean end;
        Group group;

        for (int i = 0; i < list.size(); i++) {
            end = i >= list.size() - 1;
            group = list.get(i);

            builder.append(format.replace("%group%", group.getGroupName()).replace("%priority%", String.valueOf(group.getPriority())));
            if (!end && splitter != null) builder.append(splitter);
        }

        return builder.toString();
    }
}
