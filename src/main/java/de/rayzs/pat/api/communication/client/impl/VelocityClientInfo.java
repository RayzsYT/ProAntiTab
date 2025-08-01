package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.impl.VelocityClient;
import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.plugin.VelocityLoader;

public class VelocityClientInfo extends ClientInfo {

    public VelocityClientInfo(String serverId) {
        super(serverId);
    }

    public VelocityClientInfo(String serverId, String name) {
        super(serverId, name);
    }

    @Override
    public void sendBytes(byte[] bytes) {
        VelocityLoader.getServer().getAllServers().stream().filter(server ->
                server.getServerInfo().getName().equalsIgnoreCase(getName())
        ).forEach(server ->
                server.sendPluginMessage(VelocityClient.getIdentifier(), bytes)
        );
    }
}
