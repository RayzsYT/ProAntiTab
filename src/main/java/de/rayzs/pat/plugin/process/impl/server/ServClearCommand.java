package de.rayzs.pat.plugin.process.impl.server;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ServClearCommand extends ProCommand {

    private final ExpireCache<UUID, String> CONFIRMATION = new ExpireCache<>(4, TimeUnit.SECONDS);

    public ServClearCommand() {
        super(
                "clear",
                "clr"
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
            final String confirmationString = "clear";

            if (!CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {
                CONFIRMATION.put(sender.getUniqueId(), confirmationString);
                sender.sendMessage(
                        Storage.ConfigSections.Messages.BLACKLIST.CLEAR_SERVER_CONFIRM
                                .replace("%server%", serverName)
                );

                return true;
            }

            Storage.Blacklist.getServerBlacklist(serverName).clear().save();
            Storage.handleChange(serverName);

            CONFIRMATION.remove(sender.getUniqueId());

            sender.sendMessage(
                    Storage.ConfigSections.Messages.BLACKLIST.CLEAR_SERVER
                            .replace("%server%", serverName)
            );
            return true;
        }

        String groupName = args[0];
        Group group = GroupManager.getGroupByName(groupName);

        if (group == null) {

            sender.sendMessage(
                    Storage.ConfigSections.Messages.GROUP.DOES_NOT_EXIST_SERVER
                            .replace("%group%", groupName)
                            .replace("%server%", serverName)
            );

            return true;
        }

        groupName = group.getGroupName();

        final String confirmationString = "clear " + groupName;

        if (!CONFIRMATION.getOrDefault(sender.getUniqueId(), "").equals(confirmationString)) {

            CONFIRMATION.put(sender.getUniqueId(), confirmationString);

            sender.sendMessage(
                    Storage.ConfigSections.Messages.GROUP.CLEAR_SERVER_CONFIRM
                            .replace("%group%", groupName)
                            .replace("%server%", serverName)
            );

            return true;
        }

        group.clear(serverName);
        Storage.handleChange(serverName);

        CONFIRMATION.remove(sender.getUniqueId());

        sender.sendMessage(
                Storage.ConfigSections.Messages.GROUP.CLEAR_SERVER
                        .replace("%group%", groupName)
                        .replace("%server%", serverName)
        );
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        final int length = args.length;
        return length < 2 ? Storage.Blacklist.getBlacklists().stream().map(Map.Entry::getKey).toList() :
                length < 3 ? GroupManager.getGroupNames() : null;
    }
}
