package de.rayzs.pat.api.communication;

import de.rayzs.pat.api.communication.impl.VelocityClient;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.TimeConverter;

public class ClientInfo {

    private String id, name;

    private boolean sentFeedback = false;
    private long syncTime = System.currentTimeMillis();

    private final Object serverObj;

    public ClientInfo(Object serverObj, String serverId) {
        this.serverObj = serverObj;
        this.id = serverId;
    }

    public ClientInfo(Object serverObj, String serverId, String name) {
        this.serverObj = serverObj;
        this.id = serverId;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String serverId) {
        this.id = serverId;
    }

    public void sendBytes(byte[] bytes) {
        if(Reflection.isVelocityServer()) ((com.velocitypowered.api.proxy.server.RegisteredServer) serverObj).sendPluginMessage(VelocityClient.getIdentifier(), bytes);
        else if(Reflection.isProxyServer()) ((net.md_5.bungee.api.config.ServerInfo) serverObj).sendData(Client.CHANNEL_NAME, bytes);
    }

    public String getName() {
        return name;
    }

    public boolean compareId(String id) {
        return this.id.equals(id);
    }

    public void syncTime() {
        syncTime = System.currentTimeMillis();
    }

    public String getSyncTime() {
        return TimeConverter.calcAndGetTime(syncTime);
    }

    public void setFeedback(boolean state) {
        this.sentFeedback = state;
    }

    public boolean hasSentFeedback() {
        return sentFeedback;
    }
}
