package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeleteGroupCommand extends ProCommand {

    private final ExpireCache<UUID, String> CONFIRMATION = new ExpireCache<>(4, TimeUnit.SECONDS);

    public DeleteGroupCommand() {
        super(
                "deletegroup",
                "dg"
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
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", groupName));
            return true;
        }

        groupName = group.getGroupName();

        String confirmationString = "deletegroup " + groupName;

        if (!CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
            CONFIRMATION.put(sender.getUniqueId(), confirmationString);
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DELETE_CONFIRM);
            return true;
        }

        GroupManager.unregisterGroup(groupName);
        CONFIRMATION.remove(sender.getUniqueId());

        String message = registered ? Storage.ConfigSections.Messages.GROUP.DELETE : Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST;
        message = StringUtils.replace(message, "%group%", groupName);

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }
}
