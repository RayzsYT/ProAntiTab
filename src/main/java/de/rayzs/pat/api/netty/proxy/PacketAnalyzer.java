package de.rayzs.pat.api.netty.proxy;

import de.rayzs.pat.api.netty.proxy.handlers.VelocityPacketHandler;
import de.rayzs.pat.api.netty.proxy.handlers.BungeePacketHandler;
import de.rayzs.pat.utils.Reflection;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketAnalyzer {

    public static final ConcurrentHashMap<UUID, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String PIPELINE_NAME = "pat-packethandler", HANDLER_NAME = "packet_handler";
    private static final PacketHandler PACKET_HANDLER = Reflection.isVelocityServer() ? new VelocityPacketHandler() : new BungeePacketHandler();
    private static final HashMap<UUID, String> PLAYER_INPUT_CACHE = new HashMap<>();

    public static void uninjectAll() {
        PacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(PacketAnalyzer::uninject);
        PacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static boolean inject(UUID uuid, Channel channel) {
        try {
            if(channel == null) {
                System.err.println("Failed to inject " + uuid + "! Channel is null.");
                return false;
            }

            channel.pipeline().addBefore(PacketAnalyzer.HANDLER_NAME, PacketAnalyzer.PIPELINE_NAME, new PacketDecoder(uuid));
            PacketAnalyzer.INJECTED_PLAYERS.put(uuid, channel);
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
                    if (pipeline.names().contains(PacketAnalyzer.PIPELINE_NAME)) pipeline.remove(PacketAnalyzer.PIPELINE_NAME);
                });
            }
        }
    }

    public static String getUUIDInput(UUID uuid) {
        String input = PLAYER_INPUT_CACHE.get(uuid);
        PLAYER_INPUT_CACHE.remove(uuid);
        return input;
    }

    public static void insertPlayerInput(UUID uuid, String text) {
        PLAYER_INPUT_CACHE.put(uuid, text);
    }

    private static class PacketDecoder extends ChannelDuplexHandler {

        private final UUID uuid;
        private PacketDecoder(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public void channelRead(ChannelHandlerContext channel, Object packetObj) {
            try {
                super.channelRead(channel, packetObj);
            } catch (Throwable exception) { exception.printStackTrace(); }
        }

        @Override
        public void write(ChannelHandlerContext channel, Object packetObj, ChannelPromise promise) {
            try {
                super.write(channel, packetObj, promise);
            } catch (Throwable exception) { exception.printStackTrace(); }
        }
    }
}
