package de.rayzs.pat.plugin.process.impl.local.info;

import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import java.util.*;

public class PermsCommand extends ProCommand {

    public PermsCommand() {
        super(
                "perms",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0)
            return false;

        String name = args[0];
        UUID uuid = Storage.getLoader().getUUIDByName(name);

        boolean online = uuid != null;

        if (online) {
            name = Storage.getLoader().getNameByUUID(uuid);
        }

        sender.sendMessage((online
                        ? Storage.ConfigSections.Messages.PERMS_CHECK.MESSAGE.replace("%permissions%", PermissionUtil.getPermissionsAsString(uuid))
                        : Storage.ConfigSections.Messages.PERMS_CHECK.PLAYER_NOT_ONLINE
                ).replace("%player%", name)
        );

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? Storage.getLoader().getPlayerNames() : null;
    }
}
