package de.rayzs.pat.api.netty.proxy.handlers;

import de.rayzs.pat.api.netty.proxy.PacketHandler;
import java.util.UUID;

public class BungeePacketHandler implements PacketHandler {

    @Override
    public boolean handleIncomingPacket(UUID uuid, Object packetObj) throws Exception {
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(UUID uuid, Object packetObj) throws Exception {
        return true;
    }
}