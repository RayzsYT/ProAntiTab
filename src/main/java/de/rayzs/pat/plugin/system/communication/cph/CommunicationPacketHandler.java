package de.rayzs.pat.plugin.system.communication.cph;

import de.rayzs.pat.utils.CommunicationPackets;

public interface CommunicationPacketHandler {
    void handleReceivedPacket(CommunicationPackets.PATPacket packet);
}
