package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;

public class CreateGroupCommand extends ProCommand {

    public CreateGroupCommand() {
        super(
                "creategroup",
                "cg"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            return false;
        }

        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        if (backend) {
            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
            return true;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);
        boolean registered = group != null;

        if (!registered) {
            GroupManager.registerGroup(groupName);
        } else groupName = group.getGroupName();

        String message = registered ? Storage.ConfigSections.Messages.GROUP.ALREADY_EXIST : Storage.ConfigSections.Messages.GROUP.CREATE;
        message = StringUtils.replace(message, "%group%", groupName);

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }
}
