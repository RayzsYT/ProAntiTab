package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.api.event.PATEvent;
import java.util.UUID;

public abstract class ReceiveSyncEvent extends PATEvent<ReceiveSyncEvent> {

    private final CommunicationPackets.PacketBundle packetBundle;

    public ReceiveSyncEvent() {
        super(null);
        this.packetBundle = null;
    }

    public ReceiveSyncEvent(Object senderObj, CommunicationPackets.PacketBundle packetBundle) {
        super(senderObj);
        this.packetBundle = packetBundle;
    }

    public CommunicationPackets.PacketBundle getPacketBundle() {
        return packetBundle;
    }
}
