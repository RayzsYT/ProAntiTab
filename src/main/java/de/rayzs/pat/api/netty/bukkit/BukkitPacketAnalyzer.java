package de.rayzs.pat.api.netty.bukkit;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.netty.bukkit.handlers.*;
import java.util.concurrent.ConcurrentHashMap;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitPacketAnalyzer {

    public static final ConcurrentHashMap<UUID, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();
    private static final ExpireCache<UUID, Object> SENT_PACKET = new ExpireCache<>(2, TimeUnit.SECONDS);

    private static final String HANDLER_NAME = "pat-bukkit-handler", PIPELINE_NAME = "packet_handler";
    private static final BukkitPacketHandler PACKET_HANDLER = Reflection.getMinor() >= 16 ? new ModernPacketHandler() : new LegacyPacketHandler();
    private static final HashMap<Player, String> PLAYER_INPUT_CACHE = new HashMap<>();

    public static void injectAll() {
        Bukkit.getOnlinePlayers().forEach(BukkitPacketAnalyzer::inject);
    }

    public static void uninjectAll() {
        BukkitPacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(BukkitPacketAnalyzer::uninject);
        BukkitPacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static void sendPacket(UUID uuid, Object object) {
        Channel channel = INJECTED_PLAYERS.get(uuid);

        if (channel == null)
            return;

        SENT_PACKET.put(uuid, object);
        channel.pipeline().writeAndFlush(object);
    }

    public static boolean inject(Player player) {
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            return true;

        try {
            Channel channel = Reflection.getPlayerChannel(player);

            if (channel == null) {
                return false;
            }

            if (channel.pipeline().names().contains(BukkitPacketAnalyzer.HANDLER_NAME))
                uninject(channel);

            channel.pipeline().addBefore(BukkitPacketAnalyzer.PIPELINE_NAME, BukkitPacketAnalyzer.HANDLER_NAME, new PacketDecoder(player));
            BukkitPacketAnalyzer.INJECTED_PLAYERS.put(player.getUniqueId(), channel);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Reflection.toggleInjectionMethod();

            Logger.info("Injection failed. Switching to secondary injection method.");
            boolean success = inject(player);

            if (success) {
                Logger.info("Secondary injection method was successful.");
            } else {
                Logger.warning("Secondary injection method failed!");
            }

            return success;

        } catch (Exception exception) {
            exception.printStackTrace();
            return false;

        }

        return true;
    }

    public static void uninject(UUID uuid) {
        if (Storage.SEND_CONSOLE_NOTIFICATION)
            return;

        if (BukkitPacketAnalyzer.INJECTED_PLAYERS.containsKey(uuid)) {
            Channel channel = BukkitPacketAnalyzer.INJECTED_PLAYERS.get(uuid);
            uninject(channel);
        }
    }

    public static void uninject(Channel channel) {
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            return;

        if (channel != null) {
            channel.eventLoop().submit(() -> {
                ChannelPipeline pipeline = channel.pipeline();

                if (pipeline.names().contains(BukkitPacketAnalyzer.HANDLER_NAME))
                    pipeline.remove(BukkitPacketAnalyzer.HANDLER_NAME);
            });
        }
    }

    public static String getPlayerInput(Player player) {
        String input = PLAYER_INPUT_CACHE.get(player);
        PLAYER_INPUT_CACHE.remove(player);
        return input;
    }

    public static void insertPlayerInput(Player player, String text) {
        PLAYER_INPUT_CACHE.put(player, text);
    }

    private static class PacketDecoder extends ChannelDuplexHandler {

        private final Player player;
        private PacketDecoder(Player player) {
            this.player = player;
        }

        @Override
        public void channelRead(ChannelHandlerContext channel, Object packetObj) {
            try {
                if (packetObj.getClass() == null || Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
                    super.channelRead(channel, packetObj);
                    return;
                }

                String packetName = packetObj.getClass().getSimpleName();

                if (!packetName.equals("PacketPlayInTabComplete") && !packetName.equals("ServerboundCommandSuggestionPacket")) {
                    super.channelRead(channel, packetObj);
                    return;
                }

                if (!PermissionUtil.hasBypassPermission(player)) {

                    if (!PACKET_HANDLER.handleIncomingPacket(player, packetObj))
                        return;
                }


                super.channelRead(channel, packetObj);
            } catch (Throwable exception) { exception.printStackTrace(); }
        }

        @Override
        public void write(ChannelHandlerContext channel, Object packetObj, ChannelPromise promise) {
            try {

                if(packetObj.getClass() == null || Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
                    super.write(channel, packetObj, promise);
                    return;
                }

                String packetName = packetObj.getClass().getSimpleName();

                if (!packetName.equals("PacketPlayOutTabComplete") && !packetName.equals("ClientboundCommandSuggestionsPacket")) {
                    super.write(channel, packetObj, promise);
                    return;
                }

                if (!PermissionUtil.hasBypassPermission(player)) {

                    UUID uuid = player.getUniqueId();
                    Object sentPacketObj = SENT_PACKET.get(uuid);

                    if (sentPacketObj != null) {
                        if (sentPacketObj == packetObj) {
                            SENT_PACKET.remove(uuid);
                            super.write(channel, sentPacketObj, promise);
                            return;
                        }
                    }

                    if (!PACKET_HANDLER.handleOutgoingPacket(player, packetObj))
                        return;
                }

                super.write(channel, packetObj, promise);

            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }
    }
}
