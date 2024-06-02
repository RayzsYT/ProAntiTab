package de.rayzs.pat.api.netty.proxy.handlers;

import de.rayzs.pat.api.netty.proxy.PacketAnalyzer;
import de.rayzs.pat.api.netty.proxy.PacketHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VelocityPacketHandler implements PacketHandler {

    @Override
    public boolean handleIncomingPacket(UUID uuid, Object packetObj) throws Exception {
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(UUID uuid, Object packetObj) throws Exception {
        return true;
    }
}
