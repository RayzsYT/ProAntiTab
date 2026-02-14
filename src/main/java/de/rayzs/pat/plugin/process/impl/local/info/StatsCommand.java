package de.rayzs.pat.plugin.process.impl.local.info;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

        final Set<ClientInfo> clients = Communicator.get().getClients();
        final Iterator<ClientInfo> iterator = clients.iterator();

        boolean tmpFound = false;
        StringBuilder statsBuilder = new StringBuilder();

        while (iterator.hasNext()) {
            ClientInfo client = iterator.next();

            if (!tmpFound) {
                tmpFound = true;
            }

            statsBuilder.append(StringUtils.replace(Storage.ConfigSections.Messages.STATS.SERVER,
                    "%updated%", client.getFormattedSyncTime(),
                    "%servername%", client.getServerName(),
                    "%last_alive_response%", client.getFormattedLastReceivedKeepAlivePacketTime()
            ));

            if (iterator.hasNext()) {
                statsBuilder.append(Storage.ConfigSections.Messages.STATS.SPLITTER);
            }
        }

        final boolean found = tmpFound;

        Storage.ConfigSections.Messages.STATS.STATISTIC.getLines().forEach(message -> {

            message = StringUtils.replace(message,
                    "%server_count%", String.valueOf(Communicator.get().getClients().size()),
                    "%last_sync_time%", TimeConverter.calcAndGetTime(Communicator.get().getLastSync()),
                    "%last_alive_response_time%", TimeConverter.calcAndGetTime(Communicator.get().getLastReceivedKeepAliveResponse()),
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
