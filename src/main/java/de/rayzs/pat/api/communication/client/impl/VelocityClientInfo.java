package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.impl.VelocityClient;

public class VelocityClientInfo extends ClientInfo {

    public VelocityClientInfo(Object serverObj, String serverId) {
        super(serverObj, serverId);
    }

    public VelocityClientInfo(Object serverObj, String serverId, String name) {
        super(serverObj, serverId, name);
    }

    @Override
    public void sendBytes(byte[] bytes) {
        ((com.velocitypowered.api.proxy.server.RegisteredServer) getServerObj()).sendPluginMessage(VelocityClient.getIdentifier(), bytes);
    }

    @Override
    public Object getServerObj() {
        return super.getServerObj();
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getSyncTime() {
        return super.getSyncTime();
    }

    @Override
    public void setFeedback(boolean state) {
        super.setFeedback(state);
    }

    @Override
    public void setId(String serverId) {
        super.setId(serverId);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }
}
