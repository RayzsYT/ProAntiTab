package de.rayzs.pat.plugin.process.impl.local.info;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import java.util.List;

public class StatsCommand extends ProCommand {

    public StatsCommand() {
        super(
                "stats",
                ""
        );

        proxyOnly = true;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

        if(backend) {
            sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
            return true;
        }

        final int length = Communicator.CLIENTS.size() - 1;
        boolean tmpFound = false;

        StringBuilder statsBuilder = new StringBuilder();

        for (int i = 0; i < Communicator.CLIENTS.size(); i++) {
            ClientInfo client = Communicator.CLIENTS.get(i);

            if (!client.hasSentFeedback())
                continue;

            if (!tmpFound)
                tmpFound = true;

            statsBuilder.append(StringUtils.replace(Storage.ConfigSections.Messages.STATS.SERVER,
                    "%updated%", client.getSyncTime(), "%servername%", client.getName()
            ));

            if (i <= length)
                statsBuilder.append(Storage.ConfigSections.Messages.STATS.SPLITTER);
        }

        final boolean found = tmpFound;

        Storage.ConfigSections.Messages.STATS.STATISTIC.getLines().forEach(message -> {

            message = StringUtils.replace(message,
                    "%server_count%", String.valueOf(Communicator.SERVER_DATA_SYNC_COUNT),
                    "%last_sync_time%", TimeConverter.calcAndGetTime(Communicator.LAST_DATA_UPDATE),
                    "%servers%", found ? statsBuilder.toString() : Storage.ConfigSections.Messages.STATS.NO_SERVER
            );

            sender.sendMessage(message);
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
