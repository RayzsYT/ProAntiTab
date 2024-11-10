package de.rayzs.pat.api.netty.proxy;

import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.proxy.crypto.SignedChatCommand;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.UnsignedPlayerCommandPacket;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.listeners.velocity.VelocityBlockCommandListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.proxy.protocol.packet.*;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import io.netty.channel.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

public class VelocityPacketAnalyzer {

    public static final ConcurrentHashMap<Player, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String PIPELINE_NAME = "pat-velocity-handler", HANDLER_NAME = "handler";
    private static final HashMap<Player, String> PLAYER_INPUT_CACHE = new HashMap<>();
    private static HashMap<String, CommandsCache> COMMANDS_CACHE_MAP = new HashMap<>();

    private static Class<?> minecraftConnectionClass, connectedPlayerConnectionClass, signedChatCommandPacketClass;

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

            channel = (Channel) Reflection.getMethodsByName(minecraftConnectionClass, "getChannel").get(0).invoke(minecraftConnectionObj);

            if(channel == null) {
                System.out.println("Failed to inject " + player.getUsername() + "! Channel is null.");
                return false;
            }

            if(channel.pipeline().names().contains(VelocityPacketAnalyzer.PIPELINE_NAME))
                uninject(player);

            channel.pipeline().addBefore(VelocityPacketAnalyzer.HANDLER_NAME, VelocityPacketAnalyzer.PIPELINE_NAME, new PacketDecoder(player));
            VelocityPacketAnalyzer.INJECTED_PLAYERS.put(player, channel);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        } return true;
    }

    public static void uninject(Player player) {
        VelocityPacketAnalyzer.PLAYER_INPUT_CACHE.remove(player);

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
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof MinecraftPacket)) {
                super.channelRead(ctx, msg);
                return;
            }

            MinecraftPacket packet = (MinecraftPacket) msg;

            if(packet.getClass().getSimpleName().equals("SignedChatCommand")) {
                try {
                    if(signedChatCommandPacketClass == null)
                        signedChatCommandPacketClass = Class.forName("com.velocitypowered.proxy.crypto.SignedChatCommand");

                    Object signedChatCommandPacket = signedChatCommandPacketClass.cast(packet);
                    Field commandField = signedChatCommandPacket.getClass().getField("command");
                    String command = (String) commandField.get(signedChatCommandPacket);

                    if(!VelocityBlockCommandListener.handleCommand(player, command))
                        return;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            if(packet instanceof UnsignedPlayerCommandPacket) {

                UnsignedPlayerCommandPacket unsignedPlayerCommandPacket = (UnsignedPlayerCommandPacket) packet;
                if(VelocityBlockCommandListener.handleCommand(player, unsignedPlayerCommandPacket.getCommand()))
                    return;
            }

            if(packet instanceof TabCompleteRequestPacket) {
                TabCompleteRequestPacket request = (TabCompleteRequestPacket) msg;
                if(request.getCommand() != null) {

                    if(Storage.ConfigSections.Settings.PATCH_EXPLOITS.isMalicious(request.getCommand())) {
                        MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), Storage.ConfigSections.Settings.PATCH_EXPLOITS.ALERT_MESSAGE.get().replace("%player%", player.getUsername()));
                        player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(Storage.ConfigSections.Settings.PATCH_EXPLOITS.KICK_MESSAGE.get()));
                    } else {
                        insertPlayerInput(player, request.getCommand());
                        super.channelRead(ctx, msg);
                    }
                }
            } else super.channelRead(ctx, msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (!(msg instanceof MinecraftPacket)) {
                super.write(ctx, msg, promise);
                return;
            }

            MinecraftPacket packet = (MinecraftPacket) msg;

            if(packet instanceof PluginMessagePacket) {
                PluginMessagePacket pluginMessagePacket = (PluginMessagePacket) packet;
                if(CustomServerBrand.isEnabled() && CustomServerBrand.isBrandTag(pluginMessagePacket.getChannel()) && !player.getCurrentServer().isPresent()) {
                    PacketUtils.BrandManipulate brandManipulatePacket = CustomServerBrand.createBrandPacket(player);
                    super.write(ctx, new PluginMessagePacket(pluginMessagePacket.getChannel(), brandManipulatePacket.getByteBuf()), promise);
                    return;
                }

            } else if(packet instanceof TabCompleteResponsePacket) {
                if (!PermissionUtil.hasBypassPermission(player) && player.getCurrentServer().isPresent()) {
                    TabCompleteResponsePacket response = (TabCompleteResponsePacket) packet;

                    boolean cancelsBeforeHand = false;
                    String playerInput = getPlayerInput(player), rawPlayerInput = playerInput, server = player.getCurrentServer().get().getServerInfo().getName();
                    int spaces = 0;

                    if(playerInput.contains(" ")) {
                        String[] split = playerInput.split(" ");
                        spaces = split.length;
                        if(spaces > 0) playerInput = split[0];
                    }

                    if(!playerInput.equals("/")) {
                        cancelsBeforeHand = Storage.Blacklist.isBlocked(player, StringUtils.replaceFirst(playerInput, "/", ""), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server);
                        if(!cancelsBeforeHand) cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(StringUtils.replaceFirst(playerInput, "/", "")) || Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(StringUtils.replaceFirst(playerInput, "/", ""));
                    }

                    final String cursor = playerInput;

                    if(!cursor.startsWith("/") && spaces < 2 && player.getProtocolVersion().getProtocol() >= 754) {
                        Logger.debug("Player won't receive TabCompleteResponsePacket because the client protocol id is " + player.getProtocolVersion().getProtocol() + "! This doesn't makes sense, that's why.");
                        return;
                    }

                    if(cursor.startsWith("/")) {
                        if (spaces == 0) {
                            response.getOffers().removeIf(offer -> {
                                String command = offer.getText();
                                if (command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");

                                return Storage.Blacklist.isBlocked(player, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getCurrentServer().get().getServerInfo().getName(), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
                            });
                        } else {
                            if (cancelsBeforeHand) return;
                            List<String> suggestionsAsString = new ArrayList<>();
                            response.getOffers().forEach(offer -> suggestionsAsString.add(offer.getText()));

                            FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawPlayerInput, suggestionsAsString);
                            if(filteredTabCompletionEvent.isCancelled()) return;
                            response.getOffers().removeIf(offer -> !filteredTabCompletionEvent.getCompletion().contains(offer.getText()));

                            if(response.getOffers().isEmpty()) return;
                        }
                    }
                }

            } else if(packet instanceof AvailableCommandsPacket) {
                if (!PermissionUtil.hasBypassPermission(player) && player.getCurrentServer().isPresent()) {
                    AvailableCommandsPacket commands = (AvailableCommandsPacket) packet;

                    if(!commands.getRootNode().getChildren().isEmpty()) {
                        String serverName = player.getCurrentServer().get().getServer().getServerInfo().getName();

                        if (!COMMANDS_CACHE_MAP.containsKey(serverName))
                            COMMANDS_CACHE_MAP.put(serverName, new CommandsCache().reverse());
                        CommandsCache commandsCache = COMMANDS_CACHE_MAP.get(serverName);

                        List<String> commandsAsString = new ArrayList<>();
                        commands.getRootNode().getChildren().stream().filter(command -> command != null && command.getName() != null).forEach(command -> commandsAsString.add(command.getName()));
                        commandsCache.handleCommands(commandsAsString, serverName);

                        if (PermissionUtil.hasBypassPermission(player)) return;

                        final boolean newer = player.getProtocolVersion().getProtocol() > 340;
                        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);

                        if(commands.getRootNode().getChildren().size() == 1 && newer
                                && commands.getRootNode().getChild("args") != null
                                && commands.getRootNode().getChild("args").getChildren().isEmpty()) {

                            super.write(ctx, msg, promise);
                            return;
                        }

                        commands.getRootNode().getChildren().removeIf(command -> command == null || command.getName() == null || playerCommands.contains(command.getName()));
                    }
                }
            }

            super.write(ctx, msg, promise);
        }
    }
}