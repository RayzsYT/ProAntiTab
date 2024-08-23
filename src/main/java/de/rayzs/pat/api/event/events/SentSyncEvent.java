package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.api.event.PATEvent;
import java.util.UUID;

public abstract class SentSyncEvent extends PATEvent<SentSyncEvent> {

    private final CommunicationPackets.PacketBundle packetBundle;
    private final String serverName;

    public SentSyncEvent() {
        super(null);
        this.packetBundle = null;
        this.serverName = null;
    }

    public SentSyncEvent(UUID senderUniqueId, CommunicationPackets.PacketBundle packetBundle, String serverName) {
        super(senderUniqueId);
        this.packetBundle = packetBundle;
        this.serverName = serverName;
    }

    public String getServerName() {
        return serverName;
    }

    public CommunicationPackets.PacketBundle getPacketBundle() {
        return packetBundle;
    }
}
