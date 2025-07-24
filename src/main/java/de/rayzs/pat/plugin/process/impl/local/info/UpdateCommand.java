package de.rayzs.pat.plugin.process.impl.local.info;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import java.util.*;

public class UpdateCommand extends ProCommand {

    public UpdateCommand() {
        super(
                "update",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            PermissionUtil.reloadPermissions();
            sender.sendMessage(Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_ALL);
            return true;
        }

        String name = args[0];
        UUID uuid = Storage.getLoader().getUUIDByName(name);

        boolean exist = uuid != null;

        if (exist) {
            name = Storage.getLoader().getNameByUUID(uuid);
        }

        String message = exist
                ? Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_SPECIFIC
                : Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.PLAYER_NOT_ONLINE;

        message = message.replace("%target%", name);

        if (exist) {
            PermissionUtil.reloadPermissions(uuid);
        }

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? Storage.getLoader().getPlayerNames() : null;
    }
}
