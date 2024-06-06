package de.rayzs.pat.api.netty.proxy;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionUtil;
import io.netty.channel.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityPacketAnalyzer {

    public static final ConcurrentHashMap<Player, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String PIPELINE_NAME = "pat-velocity-handler", HANDLER_NAME = "handler";
    private static final HashMap<Player, Boolean> PLAYER_MODIFIED = new HashMap<>();
    private static final HashMap<Player, String> PLAYER_INPUT_CACHE = new HashMap<>();

    private static final List<String> PLUGIN_COMMANDS = new ArrayList<>();

    private static Class<?> minecraftConnectionClass, connectedPlayerConnectionClass;

    public static void removePlayerModifies() {
        VelocityLoader.getServer().getAllPlayers().forEach(player -> setPlayerModification(player, false));
    }

    public static void injectAll() {
        VelocityLoader.getServer().getAllPlayers().forEach(VelocityPacketAnalyzer::inject);
    }

    public static void uninjectAll() {
        VelocityPacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(VelocityPacketAnalyzer::uninject);
        VelocityPacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static boolean isInjected(Player player) {
        return INJECTED_PLAYERS.containsKey(player);
    }

    public static boolean inject(Player player) {

        if(connectedPlayerConnectionClass == null)
            connectedPlayerConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.client.ConnectedPlayer");

        if(minecraftConnectionClass == null)
            minecraftConnectionClass = Reflection.getClass("com.velocitypowered.proxy.connection.MinecraftConnection");

        Channel channel;

        try {
            Object connectedPlayerObj = connectedPlayerConnectionClass.cast(player),
                    minecraftConnectionObj = Reflection.getMethodsByName(connectedPlayerConnectionClass, "getConnection").get(0).invoke(connectedPlayerObj);

            for (Field field : Reflection.getFields(minecraftConnectionObj)) {
                System.out.println(field.getName() + " | " + field.getType().getName());
            }

            channel = (Channel) Reflection.getMethodsByName(minecraftConnectionClass, "getChannel").get(0).invoke(minecraftConnectionObj);

            if(channel == null) {
                System.err.println("Failed to inject " + player.getUsername() + "! Channel is null.");
                return false;
            }

            setPlayerModification(player, false);
            if(channel.pipeline().names().contains(VelocityPacketAnalyzer.PIPELINE_NAME))
                uninject(player);

            channel.pipeline().addAfter(VelocityPacketAnalyzer.HANDLER_NAME, VelocityPacketAnalyzer.PIPELINE_NAME, new PacketDecoder(player));
            VelocityPacketAnalyzer.INJECTED_PLAYERS.put(player, channel);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        } return true;
    }

    public static boolean isPlayerModified(Player player) {
        PLAYER_MODIFIED.putIfAbsent(player, false);
        return PLAYER_MODIFIED.get(player);
    }

    public static void setPlayerModification(Player player, boolean modified) {
        PLAYER_MODIFIED.put(player, modified);
    }

    public static void uninject(Player player) {
        PLAYER_MODIFIED.remove(player);

        if(VelocityPacketAnalyzer.INJECTED_PLAYERS.containsKey(player)) {
            Channel channel = VelocityPacketAnalyzer.INJECTED_PLAYERS.get(player);
            if(channel != null) {
                VelocityPacketAnalyzer.INJECTED_PLAYERS.remove(player);
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (pipeline.names().contains(VelocityPacketAnalyzer.PIPELINE_NAME)) pipeline.remove(VelocityPacketAnalyzer.PIPELINE_NAME);
                });
            }
        }
    }

    public static String getPlayerInput(Player player) {
        String input = PLAYER_INPUT_CACHE.get(player);
        PLAYER_INPUT_CACHE.remove(player);
        return input;
    }

    public static void insertPlayerInput(Player player, String text) {
        PLAYER_INPUT_CACHE.put(player, text.toLowerCase());
    }

    private static class PacketDecoder extends ChannelDuplexHandler {

        private final Player player;

        private PacketDecoder(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (!(msg instanceof MinecraftPacket)) {
                super.write(ctx, msg, promise);
                return;
            }

            MinecraftPacket packet = (MinecraftPacket) msg;

            if(packet instanceof TabCompleteResponsePacket) {
                if (!PermissionUtil.hasBypassPermission(player) && player.getCurrentServer().isPresent()) {
                    TabCompleteResponsePacket response = (TabCompleteResponsePacket) packet;
                    response.getOffers().removeIf(offer -> offer.getText().startsWith("/") && Storage.Blacklist.isBlocked(player, offer.getText(), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getCurrentServer().get().getServerInfo().getName()));
                }
            } else if(packet instanceof AvailableCommandsPacket) {
                if (!PermissionUtil.hasBypassPermission(player) && player.getCurrentServer().isPresent()) {
                    AvailableCommandsPacket commands = (AvailableCommandsPacket) packet;
                    if(!commands.getRootNode().getChildren().isEmpty()) {
                        commands.getRootNode().getChildren().removeIf(command -> {
                            if(command == null || command.getName() == null) return true;
                            String commandName = command.getName();
                            return Storage.Blacklist.isBlocked(player, commandName, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getCurrentServer().get().getServerInfo().getName());
                        });
                    }
                }
            }

            super.write(ctx, msg, promise);
        }
    }
}