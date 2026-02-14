package de.rayzs.pat.api.communication;

import de.rayzs.pat.utils.CommunicationPackets;

public interface Client {
    String CHANNEL_NAME = "pat:channel";
    void reload();
    void send(CommunicationPackets.PATPacket packet);
}
