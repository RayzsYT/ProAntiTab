package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.api.event.PATEvent;

public abstract class ReceiveSyncEvent extends PATEvent<ReceiveSyncEvent> {

    private final CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket;

    public ReceiveSyncEvent() {
        super(null);
        this.dataSyncPacket = null;
    }

    public ReceiveSyncEvent(Object senderObj, CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket) {
        super(senderObj);
        this.dataSyncPacket = dataSyncPacket;
    }

    public CommunicationPackets.Proxy2Backend.DataSyncPacket getDataSyncPacket() {
        return dataSyncPacket;
    }
}
