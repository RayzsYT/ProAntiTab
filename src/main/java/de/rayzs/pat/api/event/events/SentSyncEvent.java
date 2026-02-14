package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.api.event.PATEvent;

public abstract class SentSyncEvent extends PATEvent<SentSyncEvent> {

    private final CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket;
    private final String serverName;

    public SentSyncEvent() {
        super(null);
        this.dataSyncPacket = null;
        this.serverName = null;
    }

    public SentSyncEvent(Object senderObj, CommunicationPackets.Proxy2Backend.DataSyncPacket dataSyncPacket, String serverName) {
        super(senderObj);
        this.dataSyncPacket = dataSyncPacket;
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public CommunicationPackets.Proxy2Backend.DataSyncPacket getDataSyncPacket() {
        return dataSyncPacket;
    }
}
