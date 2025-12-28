package de.rayzs.pat.api.netty.proxy;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.AvailableCommandsPacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import com.velocitypowered.proxy.protocol.packet.TabCompleteRequestPacket;
import com.velocitypowered.proxy.protocol.packet.TabCompleteResponsePacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.UnsignedPlayerCommandPacket;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.plugin.listeners.velocity.VelocityBlockCommandListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.PacketUtils;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.node.CommandNodeHelper;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityPacketAnalyzer {

    private static boolean FAILED = false;

    static {
        try {
            final String name = "minecraft:ask_server";

            final Object availableCommandsPacket = AvailableCommandsPacket.class.newInstance();
            final Class<?> packetInstanceObjClass = availableCommandsPacket.getClass();

            for (Class<?> clazz : packetInstanceObjClass.getClasses()) {
                if (clazz.getSimpleName().equals("ProtocolSuggestionProvider")) {
                    final Object suggestionProviderObj = clazz.getConstructor(String.class).newInstance(name);
                    final SuggestionProvider<?> suggestionProvider = (SuggestionProvider<?>) suggestionProviderObj;

                    CommandNodeHelper.setDefaultSuggestionProvider(suggestionProvider);
                    break;
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            FAILED = true;
        }
    }

    public static final ConcurrentHashMap<Player, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String PIPELINE_NAME = "pat-velocity-handler", HANDLER_NAME = "handler";
    private static final HashMap<Player, String> PLAYER_INPUT_CACHE = new HashMap<>();

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

            if (channel == null) {
                Logger.warning("Failed to inject " + player.getUsername() + "! Channel is null.");
                return false;
            }

            if (channel.pipeline().names().contains(VelocityPacketAnalyzer.PIPELINE_NAME))
                uninject(player);

            channel.pipeline().addBefore(VelocityPacketAnalyzer.HANDLER_NAME, VelocityPacketAnalyzer.PIPELINE_NAME, new PacketDecoder(player));
            VelocityPacketAnalyzer.INJECTED_PLAYERS.put(player, channel);

        } catch (NoSuchElementException e) {
            Logger.warning("Failed to find 'handler' inside player pipeline! (name=" + player.getUsername() + ", uuid=" + player.getUniqueId() + ", version=" + player.getProtocolVersion() + ", brand=" + player.getClientBrand() + " )");
            return false;

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }

        return true;
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

    private static void modifyCommands(Player player, CommandSender sender, AvailableCommandsPacket commands) {

        if (commands.getRootNode().getChildren().size() == 1
                && player.getProtocolVersion().getProtocol() > 340
                && commands.getRootNode().getChild("args") != null
                && commands.getRootNode().getChild("args").getChildren().isEmpty()) {

            return;
        }


        String serverName = sender.getServerName();

        final boolean ignore = PermissionUtil.hasBypassPermission(sender) || Storage.Blacklist.isDisabledServer(serverName);
        final CommandNodeHelper<CommandSource> helper = new CommandNodeHelper<>(commands.getRootNode());

        final List<String> commandsAsString = new ArrayList<>();
        commandsAsString.addAll(helper.getChildrenNames());

        if (ignore) {
            return;
        }

        final List<Group> groups = GroupManager.getPlayerGroups(sender);

        final Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();
        if (!cache.containsKey(serverName)) {
            cache.put(serverName, new CommandsCache());
        }

        final CommandsCache commandsCache = cache.get(serverName);
        commandsCache.handleCommands(commandsAsString, serverName);

        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, sender, groups, serverName);
        helper.removeIf(str -> {
            if (str.equals("args")) {
                return false;
            }

            return !playerCommands.contains(str);
        });

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.ALWAYS_TAB_COMPLETABLE) {
            Storage.ConfigSections.Settings.CUSTOM_VERSION.COMMANDS.getLines().forEach(input -> helper.add(input, false));
        }

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.ALWAYS_TAB_COMPLETABLE) {
            Storage.ConfigSections.Settings.CUSTOM_PLUGIN.COMMANDS.getLines().forEach(input -> helper.add(input, false));
        }

        SubArgsModule.handleCommandNode(player.getUniqueId(), helper);
    }

    private static class PacketDecoder extends ChannelDuplexHandler {

        private final Player player;
        private final CommandSender sender;

        private PacketDecoder(Player player) {
            this.player = player;
            this.sender = CommandSenderHandler.from(player);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!(msg instanceof MinecraftPacket)) {
                super.channelRead(ctx, msg);
                return;
            }

            MinecraftPacket packet = (MinecraftPacket) msg;

            if (packet.getClass().getSimpleName().equals("SignedChatCommand")) {

                try {
                    if (signedChatCommandPacketClass == null)
                        signedChatCommandPacketClass = Class.forName("com.velocitypowered.proxy.crypto.SignedChatCommand");

                    Object signedChatCommandPacket = signedChatCommandPacketClass.cast(packet);
                    Field commandField = signedChatCommandPacket.getClass().getField("command");
                    String command = (String) commandField.get(signedChatCommandPacket);

                    if (!VelocityBlockCommandListener.handleCommand(player, command).getResult().isAllowed())
                        return;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            if (packet instanceof UnsignedPlayerCommandPacket unsignedPlayerCommandPacket) {
                if (!VelocityBlockCommandListener.handleCommand(player, unsignedPlayerCommandPacket.getCommand()).getResult().isAllowed())
                    return;
            }

            if (packet instanceof TabCompleteRequestPacket request) {
                if (request.getCommand() != null) {

                    if (Storage.ConfigSections.Settings.PATCH_EXPLOITS.isMalicious(request.getCommand())) {
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

            if (packet instanceof PluginMessagePacket pluginMessagePacket) {
                if(CustomServerBrand.isEnabled() && CustomServerBrand.isBrandTag(pluginMessagePacket.getChannel()) && !player.getCurrentServer().isPresent()) {
                    PacketUtils.BrandManipulate brandManipulatePacket = CustomServerBrand.createBrandPacket(player);
                    super.write(ctx, new PluginMessagePacket(pluginMessagePacket.getChannel(), brandManipulatePacket.getByteBuf()), promise);
                    return;
                }

            } else if (packet instanceof TabCompleteResponsePacket response) {

                String serverName = player.getCurrentServer().get().getServer().getServerInfo().getName();

                if (Storage.Blacklist.isDisabledServer(serverName)) {
                    super.write(ctx, msg, promise);
                    return;
                }

                if (!PermissionUtil.hasBypassPermission(sender) && player.getCurrentServer().isPresent()) {
                    final List<Group> groups = GroupManager.getPlayerGroups(sender);

                    boolean cancelsBeforeHand = false;
                    String playerInput = getPlayerInput(player), rawPlayerInput = playerInput, server = player.getCurrentServer().get().getServerInfo().getName();
                    int spaces = 0;

                    if (playerInput.contains(" ")) {
                        String[] split = playerInput.split(" ");
                        spaces = split.length;
                        if (spaces > 0) playerInput = split[0];
                    }

                    if (!playerInput.equals("/")) {
                        cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(sender, groups, StringUtils.replaceFirst(playerInput, "/", ""), server);

                        if (!cancelsBeforeHand)
                            cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", "")) || Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", ""));
                    }

                    final String cursor = playerInput;

                    if (!cursor.startsWith("/") && spaces < 2 && player.getProtocolVersion().getProtocol() >= 754) {
                        return;
                    }

                    if (cursor.startsWith("/")) {
                        if (spaces == 0) {
                            response.getOffers().removeIf(offer -> {
                                String command = offer.getText();
                                if (command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");

                                if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(command) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(command)) {
                                    return false;
                                }

                                return !Storage.Blacklist.canPlayerAccessTab(sender, groups, command, player.getCurrentServer().get().getServerInfo().getName());
                            });
                        } else {
                            if (cancelsBeforeHand) return;

                            List<String> suggestionsAsString = new ArrayList<>();

                            response.getOffers().forEach(offer -> suggestionsAsString.add(offer.getText()));

                            FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawPlayerInput, suggestionsAsString);
                            if (filteredTabCompletionEvent.isCancelled()) {
                                return;
                            }

                            response.getOffers().removeIf(offer -> !filteredTabCompletionEvent.getCompletion().contains(offer.getText()));

                            if (response.getOffers().isEmpty()) return;
                        }
                    }
                }

            } else if (packet instanceof AvailableCommandsPacket commands) {

                if (player.getCurrentServer().isPresent()) {

                    if (!FAILED) {
                        modifyCommands(player, sender, commands);
                        super.write(ctx, msg, promise);
                        return;
                    }

                    if (!PermissionUtil.hasBypassPermission(player)) {
                        super.write(ctx, msg, promise);
                        return;
                    }

                    final String serverName = player.getCurrentServer().get().getServer().getServerInfo().getName();



                    if (Storage.Blacklist.isDisabledServer(serverName)) {
                        super.write(ctx, msg, promise);
                        return;
                    }

                    final List<Group> groups = GroupManager.getPlayerGroups(sender);

                    if(!commands.getRootNode().getChildren().isEmpty()) {
                        Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();

                        if (!cache.containsKey(serverName))
                            cache.put(serverName, new CommandsCache());

                        CommandsCache commandsCache = cache.get(serverName);

                        List<String> commandsAsString = new ArrayList<>();
                        commands.getRootNode().getChildren().stream().filter(command -> command != null && command.getName() != null).forEach(command -> commandsAsString.add(command.getName()));
                        commandsCache.handleCommands(commandsAsString, serverName);

                        if (PermissionUtil.hasBypassPermission(player)) return;

                        final boolean newer = player.getProtocolVersion().getProtocol() > 340;
                        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, sender, groups, serverName);

                        if (commands.getRootNode().getChildren().size() == 1 && newer
                                && commands.getRootNode().getChild("args") != null
                                && commands.getRootNode().getChild("args").getChildren().isEmpty()) {

                            super.write(ctx, msg, promise);
                            return;
                        }

                        commands.getRootNode().getChildren().removeIf(command -> {
                            if (command == null || command.getName() == null)
                                return true;

                            if (command.getName().equals("args"))
                                return false;

                            if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(command.getName()) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(command.getName())) {
                                return false;
                            }

                            return !playerCommands.contains(command.getName());
                        });
                    }
                }
            }

            super.write(ctx, msg, promise);
        }
    }
}