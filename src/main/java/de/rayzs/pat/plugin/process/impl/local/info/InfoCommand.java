package de.rayzs.pat.plugin.process.impl.local.info;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.List;

public class InfoCommand extends ProCommand {

    public InfoCommand() {
        super(
                "info",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        String message = StringUtils.getStringList(Storage.ConfigSections.Messages.INFO.MESSAGE.getLines(), "\n");

        String syncTime =  Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || Reflection.isProxyServer()
                ? Storage.ConfigSections.Messages.INFO.SYNC_TIME.replace("%time%", TimeConverter.calcAndGetTime(Reflection.isProxyServer()
                ? Communicator.LAST_DATA_UPDATE : Communicator.LAST_BUKKIT_SYNC))
                : Storage.ConfigSections.Messages.INFO.SYNC_DISABLED;

        String versionStatus = Storage.OUTDATED
                ? Storage.ConfigSections.Messages.INFO.VERSION_OUTDATED
                : Storage.ConfigSections.Messages.INFO.VERSION_UPDATED;

        message = StringUtils.replace(message,
                "%sync_time%", syncTime,
                "%version_status%", versionStatus
        );

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
