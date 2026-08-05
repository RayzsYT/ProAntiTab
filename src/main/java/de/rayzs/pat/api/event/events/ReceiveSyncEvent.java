package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

public abstract class ReceiveSyncEvent extends PATEvent<ReceiveSyncEvent> {

    private final CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket;

    public ReceiveSyncEvent() {
        super(null);
        this.dataSyncPacket = null;
    }

    public ReceiveSyncEvent(CommandSender player, CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket) {
        super(player);

        this.dataSyncPacket = dataSyncPacket;
    }

    public CommunicationPackets.Proxy2Backend.DataSyncPacket getDataSyncPacket() {
        return dataSyncPacket;
    }
}
