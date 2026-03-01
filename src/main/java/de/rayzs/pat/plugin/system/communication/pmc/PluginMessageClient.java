package de.rayzs.pat.plugin.system.communication.pmc;

import de.rayzs.pat.utils.CommunicationPackets;

public interface PluginMessageClient {
    String CHANNEL_NAME = "pat:channel";
    void reload();
    void send(CommunicationPackets.PATPacket packet);
}
