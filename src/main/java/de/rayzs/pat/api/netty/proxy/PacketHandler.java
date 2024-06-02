package de.rayzs.pat.api.netty.proxy;

import java.util.UUID;

public interface PacketHandler {
    boolean handleIncomingPacket(UUID uuid, Object packetObj) throws Exception;
    boolean handleOutgoingPacket(UUID uuid, Object packetObj) throws Exception;
}
