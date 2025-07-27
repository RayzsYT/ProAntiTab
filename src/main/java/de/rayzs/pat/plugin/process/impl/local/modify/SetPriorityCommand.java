package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;

public class SetPriorityCommand extends ProCommand {

    public SetPriorityCommand() {
        super(
                "setpriority",
                "sp"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        if (backend) {
            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", groupName));
            return true;
        }

        String priorityStr = args[1];

        try {
            int priority = Integer.parseInt(priorityStr);

            if (priority > 0) {
                group.setPriority(priority);

                GroupManager.sort();
                Storage.handleChange();

                sender.sendMessage(Storage.ConfigSections.Messages.GROUP.PRIORITY_SUCCESS.replace("%group%", group.getGroupName()).replace("%priority%", priorityStr));
                return true;
            }

        } catch (Exception ignored) {}

        sender.sendMessage(Storage.ConfigSections.Messages.GROUP.PRIORITY_FAILED.replace("%group%", groupName).replace("%priority%", priorityStr));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }
}
