package de.rayzs.pat.plugin.netty;

import de.rayzs.pat.plugin.netty.handlers.LegacyPacketHandler;
import de.rayzs.pat.plugin.netty.handlers.ModernPacketHandler;
import de.rayzs.pat.utils.Reflection;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketAnalyzer {

    public static final ConcurrentHashMap<UUID, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();
    public static final String CHANNEL_NAME = "pat-packethandler", HANDLER_NAME = "packet_handler";
    private static final PacketHandler PACKET_HANDLER = Reflection.getMinor() >= 18 ? new ModernPacketHandler() : new LegacyPacketHandler();

    public static void injectAll() {
        Bukkit.getOnlinePlayers().forEach(PacketAnalyzer::inject);
    }

    public static void uninjectAll() {
        PacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(PacketAnalyzer::uninject);
        PacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static boolean inject(Player player) {
        try {
            Channel channel = Reflection.getPlayerChannel(player);
            if(channel == null) {
                System.err.println("Failed to inject " + player.getName() + "! Channel is null.");
                return false;
            }

            channel.pipeline().addAfter(PacketAnalyzer.HANDLER_NAME, PacketAnalyzer.CHANNEL_NAME, new PacketDecoder(player));
            PacketAnalyzer.INJECTED_PLAYERS.put(player.getUniqueId(), channel);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        } return true;
    }

    public static void uninject(UUID uuid) {
        if(PacketAnalyzer.INJECTED_PLAYERS.containsKey(uuid)) {
            Channel channel = PacketAnalyzer.INJECTED_PLAYERS.get(uuid);
            if(channel != null) {
                PacketAnalyzer.INJECTED_PLAYERS.remove(uuid);
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (pipeline.names().contains(PacketAnalyzer.CHANNEL_NAME)) pipeline.remove(PacketAnalyzer.CHANNEL_NAME);
                });
            }
        }
    }

    private static class PacketDecoder extends ChannelDuplexHandler {

        private final Player player;
        private PacketDecoder(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext channel, Object packetObj, ChannelPromise promise) throws Exception {
            try {
                if (!player.hasPermission("proantitab.bypass") && packetObj.getClass() != null) {
                    String packetName = packetObj.getClass().getSimpleName();
                    if (packetName.equals("PacketPlayOutTabComplete"))
                        PACKET_HANDLER.handlePacket(player, packetObj);

                }
            } catch (Throwable exception) { exception.printStackTrace(); }
            super.write(channel, packetObj, promise);
        }
    }
}
