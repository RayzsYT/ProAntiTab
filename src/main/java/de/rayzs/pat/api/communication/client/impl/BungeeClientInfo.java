package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.Client;
import de.rayzs.pat.api.communication.client.ClientInfo;
import net.md_5.bungee.api.ProxyServer;

public class BungeeClientInfo extends ClientInfo {

    public BungeeClientInfo(String serverId) {
        super(serverId);
    }

    public BungeeClientInfo(String serverId, String name) {
        super(serverId, name);
    }

    @Override
    public void sendBytes(byte[] bytes) {
        ProxyServer.getInstance().getServers().entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(getName())).forEach(entry -> entry.getValue().sendData(Client.CHANNEL_NAME, bytes));
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String serverId) {
        super.setId(serverId);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String getSyncTime() {
        return super.getSyncTime();
    }

    @Override
    public void setFeedback(boolean state) {
        super.setFeedback(state);
    }
}

