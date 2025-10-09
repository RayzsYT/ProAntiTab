package de.rayzs.pat.plugin.process.impl.local.system;

import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;

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

            Storage.getLoader().getOnlinePlayerNames().forEach(name -> {
                Object playerObj = Storage.getLoader().getPlayerObjByName(name);

                if (playerObj != null) {
                    CommandSender s = CommandSenderHandler.from(playerObj);

                    assert s != null;
                    PermissionUtil.reloadPermissions(s);
                }
            });

            if (!Reflection.isProxyServer() && Reflection.getMinor() >= 13) {
                BukkitAntiTabListener.handleTabCompletion();
            }

            sender.sendMessage(Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_ALL);
            return true;
        }

        String name = args[0];
        UUID uuid = Storage.getLoader().getUUIDByName(name);
        Object playerObj = Storage.getLoader().getPlayerObjByUUID(uuid);
        CommandSender targetSender = CommandSenderHandler.from(playerObj);

        boolean exist = targetSender != null;

        if (exist) {
            name = targetSender.getName();
        }

        String message = exist
                ? Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.UPDATE_SPECIFIC
                : Storage.ConfigSections.Messages.UPDATE_PERMISSIONS.PLAYER_NOT_ONLINE;

        message = message.replace("%target%", name);

        if (exist) {

            PermissionUtil.reloadPermissions(targetSender);

            if (!Reflection.isProxyServer() && Reflection.getMinor() >= 13) {
                BukkitAntiTabListener.handleTabCompletion(uuid);
            }

        }

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return args.length < 2 ? Storage.getLoader().getOnlinePlayerNames() : null;
    }
}
