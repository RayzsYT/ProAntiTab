package de.rayzs.pat.api.communication.client;

import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.TimeConverter;

import java.util.UUID;

public abstract class ClientInfo {

    private UUID id;
    private String serverName;

    private long syncTime = System.currentTimeMillis(),
                 keepAliveTime = System.currentTimeMillis();

    public ClientInfo(UUID id) {
        this.id = id;
    }

    public ClientInfo(UUID id, String name) {
        this.id = id;
        this.serverName = name;
    }

    public abstract void send(CommunicationPackets.PATPacket packet);

    public void setServerName(String name) {
        this.serverName = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServerName() {
        return serverName;
    }

    public void updateSyncTime() {
        syncTime = System.currentTimeMillis();
    }

    public void updateKeepAliveTime() {
        keepAliveTime = System.currentTimeMillis();
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - keepAliveTime > 5000;
    }

    public String getFormattedSyncTime() {
        return TimeConverter.calcAndGetTime(syncTime);
    }

    public String getFormattedLastReceivedKeepAlivePacketTime() {
        return TimeConverter.calcAndGetTime(keepAliveTime);
    }

    public UUID getId() {
        return id;
    }
}
