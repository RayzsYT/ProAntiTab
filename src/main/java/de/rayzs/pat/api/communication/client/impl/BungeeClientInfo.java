package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.Client;
import de.rayzs.pat.api.communication.client.ClientInfo;
import net.md_5.bungee.api.connection.Server;

public class BungeeClientInfo extends ClientInfo {

    public BungeeClientInfo(Object serverObj, String serverId) {
        super(serverObj, serverId);
    }

    public BungeeClientInfo(Object serverObj, String serverId, String name) {
        super(serverObj, serverId, name);
    }

    @Override
    public void sendBytes(byte[] bytes) {
        ((Server) getServerObj()).sendData(Client.CHANNEL_NAME, bytes);
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

