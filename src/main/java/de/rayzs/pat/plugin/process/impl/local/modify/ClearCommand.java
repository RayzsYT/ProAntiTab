package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import java.util.concurrent.TimeUnit;
import de.rayzs.pat.utils.group.*;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

public class ClearCommand extends ProCommand {

    private final ExpireCache<UUID, String> CONFIRMATION = new ExpireCache<>(4, TimeUnit.SECONDS);

    public ClearCommand() {
        super(
                "clear",
                "clr"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length == 0) {
            final String confirmationString = "clear";

            if (!CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                CONFIRMATION.put(sender.getUniqueId(), confirmationString);
                sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR_CONFIRM);

                return true;
            }

            Storage.Blacklist.getBlacklist().clear().save();
            Storage.handleChange();

            CONFIRMATION.remove(sender.getUniqueId());

            sender.sendMessage(Storage.ConfigSections.Messages.BLACKLIST.CLEAR);
            return true;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST.replace("%group%", groupName));
            return true;
        }

        groupName = group.getGroupName();

        final String confirmationString = "clear " + groupName;

        if (!CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
            CONFIRMATION.put(sender.getUniqueId(), confirmationString);
            sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR_CONFIRM);
            return true;
        }

        group.clear();
        Storage.handleChange();

        CONFIRMATION.remove(sender.getUniqueId());

        sender.sendMessage(Storage.ConfigSections.Messages.GROUP.CLEAR.replace("%group%", groupName));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? GroupManager.getGroupNames() : null;
    }
}
