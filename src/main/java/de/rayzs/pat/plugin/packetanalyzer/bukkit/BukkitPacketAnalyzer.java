package de.rayzs.pat.plugin.packetanalyzer.bukkit;

import com.mojang.datafixers.kinds.Const;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.packetanalyzer.bukkit.handlers.LegacyPacketHandler;
import de.rayzs.pat.plugin.packetanalyzer.bukkit.handlers.ModernCommandsNodeHandler;
import de.rayzs.pat.plugin.packetanalyzer.bukkit.handlers.ModernPacketHandler;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import java.util.concurrent.TimeUnit;

import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;
import io.netty.channel.*;
import org.bukkit.Bukkit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BukkitPacketAnalyzer {

    public static final ConcurrentHashMap<UUID, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();
    private static final ExpireCache<UUID, Object> SENT_PACKET = new ExpireCache<>(2, TimeUnit.SECONDS);

    private static final String HANDLER_NAME = "pat-bukkit-handler", PIPELINE_NAME = "packet_handler";

    private static final BukkitPacketHandler PACKET_HANDLER = Reflection.isAtLeast(1, 16) ? new ModernPacketHandler() : new LegacyPacketHandler();
    private static final BukkitPacketHandler COMMANDS_NODE_HANDLER = new ModernCommandsNodeHandler();

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
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Storage.ConfigSections.Settings.HIDE_PLUGIN_CHANNELS.ENABLED)
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
            // Switching injection method since some versions of Leaf
            // seem to be using different logging variants.
            // It's implemented this way since some Leaf forks might have
            // changed the compilation and therefore the variables again.

            Reflection.toggleInjectionMethod();
            final boolean success = inject(player);

            if (!success) {
                Logger.warning("Both injection methods failed! Please report this back to me on my Discord! (https://www.rayzs.de/discord)");
            }

            return success;

        } catch (NoSuchFieldException noSuchFieldException) {
            Logger.warning("Failed to inject into " + player.getName() + " and kicked the player as result, to avoid any security risks.");
            Logger.warning("You can read more about it here: https://www.rayzs.de/products/proantitab/pkafi");
            Logger.warning("Error details: " + noSuchFieldException.getMessage());

            return false;

        } catch (Exception exception) {
            if (!Storage.ConfigSections.Settings.INJECTION_FAILED.SUPPRESS_EXCEPTIONS) {
                exception.printStackTrace();
            }

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
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Storage.ConfigSections.Settings.HIDE_PLUGIN_CHANNELS.ENABLED)
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
        private final CommandSender sender;

        private long lastCheckedForOperatorPermission = System.currentTimeMillis();
        private boolean operatorStatus;

        private PacketDecoder(Player player) {
            this.player = player;
            this.sender = CommandSender.from(player);

            this.operatorStatus = player.isOp();
        }

        @Override
        public void channelRead(ChannelHandlerContext channel, Object packetObj) {
            try {
                if (packetObj.getClass() == null) {
                    super.channelRead(channel, packetObj);
                    return;
                }


                String packetName = packetObj.getClass().getSimpleName();

                if (Storage.ConfigSections.Settings.HIDE_PLUGIN_CHANNELS.ENABLED && packetName.equalsIgnoreCase("ServerboundCustomPayloadPacket")) {
                    final Object newPacketObj = handlePluginChannelPacket(packetObj);

                    if (newPacketObj != null)
                        super.channelRead(channel, newPacketObj);

                    return;
                }


                if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
                    super.channelRead(channel, packetObj);
                    return;
                }


                if (!packetName.equals("PacketPlayInTabComplete") && !packetName.equals("ServerboundCommandSuggestionPacket")) {
                    super.channelRead(channel, packetObj);
                    return;
                }

                if (!hasBypassPermission() && !PACKET_HANDLER.handleIncomingPacket(player, sender, packetObj, false)) {
                    return;
                }


                super.channelRead(channel, packetObj);
            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void write(ChannelHandlerContext channel, Object packetObj, ChannelPromise promise) {
            try {
                if(packetObj.getClass() == null) {
                    super.write(channel, packetObj, promise);
                    return;
                }


                final String packetName = packetObj.getClass().getSimpleName();

                if (Storage.ConfigSections.Settings.HIDE_PLUGIN_CHANNELS.ENABLED && packetName.equalsIgnoreCase("ClientboundCustomPayloadPacket")) {
                    final Object newPacketObj = handlePluginChannelPacket(packetObj);

                    if (newPacketObj != null)
                        super.write(channel, newPacketObj, promise);

                    return;
                }


                if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
                    super.write(channel, packetObj, promise);
                    return;
                }


                final boolean isCommandsPacket = packetName.equals("ClientboundCommandsPacket");
                final boolean isTabCompletePacket = packetName.equals("PacketPlayOutTabComplete") || packetName.equals("ClientboundCommandSuggestionsPacket");

                if (!isCommandsPacket && !isTabCompletePacket || hasBypassPermission()) {
                    super.write(channel, packetObj, promise);
                    return;
                }

                if (isCommandsPacket) {
                    COMMANDS_NODE_HANDLER.handleOutgoingPacket(player, sender, packetObj, false);

                    super.write(channel, packetObj, promise);
                    return;
                }

                final UUID uuid = sender.getUniqueId();
                final Object sentPacketObj = SENT_PACKET.get(uuid);

                if (sentPacketObj != null) {
                    if (sentPacketObj == packetObj) {
                        SENT_PACKET.remove(uuid);
                        super.write(channel, sentPacketObj, promise);
                        return;
                    }
                }

                if (!PACKET_HANDLER.handleOutgoingPacket(player, sender, packetObj, false)) {
                    return;
                }

                super.write(channel, packetObj, promise);

            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }

        private boolean hasBypassPermission() {
            if (PermissionUtil.hasBypassPermission(sender, false)) {
                return true;
            }

            if (System.currentTimeMillis() - this.lastCheckedForOperatorPermission >= 2000) {
                this.lastCheckedForOperatorPermission = System.currentTimeMillis();
                this.operatorStatus = player.isOp();
                return this.operatorStatus;
            }

            return this.operatorStatus;
        }

        private static HashSet<String> REGISTER_CHANNELS = new HashSet<>(
                Arrays.asList("register", "unregister", "minecraft:register", "minecraft:unregister")
        );

        private Object handlePluginChannelPacket(Object packet) throws NoSuchFieldException, IllegalAccessException {
            final Field payloadField = packet.getClass().getDeclaredField("payload");
            payloadField.setAccessible(true);

            final Object payloadObj = payloadField.get(packet);
            final Field channelField = payloadObj.getClass().getDeclaredField("id");
            channelField.setAccessible(true);

            final Object channelIdObj = channelField.get(payloadObj);
            final String channelId = channelIdObj.toString();
            channelField.setAccessible(false);


            if (!REGISTER_CHANNELS.contains(channelId)) {
                return packet;
            }


            final Field dataField = payloadObj.getClass().getDeclaredField("data");
            dataField.setAccessible(true);

            final byte[] data = (byte[]) dataField.get(payloadObj);
            dataField.setAccessible(true);

            final String dataStr = new String(data, StandardCharsets.UTF_8);
            final String[] channels = dataStr.split("\u0000");

            final List<String> filteredChannels = new ArrayList<>(Arrays.asList(channels));
            final AtomicBoolean changedAnything = new AtomicBoolean(false);

            filteredChannels.removeIf(channel -> {
                if (!Storage.ConfigSections.Settings.HIDE_PLUGIN_CHANNELS.WHITELISTED_CHANNELS.getLines().contains(channel)) {
                    changedAnything.set(true);
                    return true;
                }

                return false;
            });

            if (!changedAnything.get()) {
                return packet;
            }

            try {
                final Class<?> customPayloadClass = packet.getClass().getConstructors()[0].getParameterTypes()[0];
                final Class<?> discardPayloadClass = payloadObj.getClass();

                final Constructor<?> discardPayloadConstr = discardPayloadClass.getConstructor(channelIdObj.getClass(), byte[].class);
                final Object newDiscardPayloadObj = discardPayloadConstr.newInstance(
                        channelIdObj,
                        String.join("\u0000", filteredChannels).getBytes()
                );

                final Constructor<?> newPacketConstr = packet.getClass().getConstructor(customPayloadClass);
                return newPacketConstr.newInstance(newDiscardPayloadObj);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return packet;
        }
    }
}
