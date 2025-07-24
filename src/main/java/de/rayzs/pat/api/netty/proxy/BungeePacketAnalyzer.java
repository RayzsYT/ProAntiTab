package de.rayzs.pat.api.netty.proxy;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import com.mojang.brigadier.tree.RootCommandNode;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.node.CommandNodeHelper;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.permission.PermissionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Commands;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;

public class BungeePacketAnalyzer {

    public static final ConcurrentHashMap<ProxiedPlayer, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String HANDLER_NAME = "pat-bungee-handler", PIPELINE_NAME = "packet-decoder";
    private static final HashMap<ProxiedPlayer, Boolean> PLAYER_MODIFIED = new HashMap<>();
    private static final HashMap<ProxiedPlayer, String> PLAYER_INPUT_CACHE = new HashMap<>();

    private static final List<String> PLUGIN_COMMANDS = new ArrayList<>();

    private static Class<?> channelWrapperClass, serverConnectionClass;

    public static void injectAll() {
        ProxyServer.getInstance().getPlayers().forEach(BungeePacketAnalyzer::inject);
    }

    public static void uninjectAll() {
        BungeePacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(BungeePacketAnalyzer::uninject);
        BungeePacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static void setPluginCommands() {
        if(!PLUGIN_COMMANDS.isEmpty()) return;
        ProxyServer.getInstance().getPluginManager().getCommands().stream().filter(entry -> !PLUGIN_COMMANDS.contains(entry.getKey())).forEach(entry -> PLUGIN_COMMANDS.add(entry.getKey()));
    }

    public static boolean inject(ProxiedPlayer player) {

        if(channelWrapperClass == null)
            channelWrapperClass = Reflection.getClass("net.md_5.bungee.netty.ChannelWrapper");

        if(serverConnectionClass == null)
            serverConnectionClass = Reflection.getClass("net.md_5.bungee.ServerConnection");

        Object channelWrapperObj;
        Field channelField;
        Channel channel;

        try {
            channelField = Reflection.getFieldByName(serverConnectionClass, "ch");
            channelWrapperObj = channelField.get(player.getServer());
            channel = (Channel) Reflection.getFieldsByType(channelWrapperClass, "Channel", Reflection.SearchOption.ENDS).get(0).get(channelWrapperObj);

            if(channel == null) {
                Logger.warning("Failed to inject " + player.getName() + "! Channel is null.");
                return false;
            }

            channelField.setAccessible(false);

            if(channel.pipeline().names().contains(BungeePacketAnalyzer.HANDLER_NAME))
                uninject(player);

            channel.pipeline().addAfter(BungeePacketAnalyzer.PIPELINE_NAME, BungeePacketAnalyzer.HANDLER_NAME, new PacketDecoder(player));
            BungeePacketAnalyzer.INJECTED_PLAYERS.put(player, channel);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        } return true;
    }

    public static void uninject(ProxiedPlayer player) {
        BungeePacketAnalyzer.PLAYER_INPUT_CACHE.remove(player);
        BungeePacketAnalyzer.PLAYER_MODIFIED.remove(player);

        if(BungeePacketAnalyzer.INJECTED_PLAYERS.containsKey(player)) {
            Channel channel = BungeePacketAnalyzer.INJECTED_PLAYERS.get(player);
            if(channel != null) {
                BungeePacketAnalyzer.INJECTED_PLAYERS.remove(player);
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (pipeline.names().contains(BungeePacketAnalyzer.HANDLER_NAME)) pipeline.remove(BungeePacketAnalyzer.HANDLER_NAME);
                });
            }
        }
    }

    public static String getPlayerInput(ProxiedPlayer player) {
        String input = PLAYER_INPUT_CACHE.get(player);
        PLAYER_INPUT_CACHE.remove(player);
        return input;
    }

    private static void modifyCommands(ProxiedPlayer player, Commands commands) {
        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo.getName();

        CommandNodeHelper helper = new CommandNodeHelper<CommandNode>(commands.getRoot());
        Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();

        if(!cache.containsKey(serverName))
            cache.put(serverName, new CommandsCache());

        CommandsCache commandsCache = cache.get(serverName);

        List<String> commandsAsString = new ArrayList<String>(helper.getChildrenNames());
        commandsCache.handleCommands(commandsAsString, serverName);

        List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId());
        helper.removeIf(str -> !playerCommands.contains(str));

        // Disabled temporarily
        // SubArgsModule.handleCommandNode(player.getUniqueId(), helper);
    }

    private static class PacketDecoder extends MessageToMessageDecoder<PacketWrapper> {

        private final ProxiedPlayer player;

        private PacketDecoder(ProxiedPlayer player) {
            this.player = player;
        }

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, PacketWrapper wrapper, List<Object> list) {
            if (wrapper.packet == null) {
                list.add(wrapper);
                return;
            }

            if(wrapper.packet instanceof PluginMessage) {
                PluginMessage pluginMessage = (PluginMessage) wrapper.packet;
                if(CustomServerBrand.isEnabled() && CustomServerBrand.isBrandTag(pluginMessage.getTag()))
                    return;

            } else if (wrapper.packet instanceof Commands) {
                Commands response = (Commands) wrapper.packet;

                modifyCommands(player, response);

                player.unsafe().sendPacket(response);
                return;

            } else if (wrapper.packet instanceof TabCompleteResponse) {
                TabCompleteResponse response = (TabCompleteResponse) wrapper.packet;

                if(player.getPendingConnection().getVersion() < 754) {
                    if(!PermissionUtil.hasBypassPermission(player)) {

                        ClientInfo clientInfo = Communicator.getClientByName(player.getServer().getInfo().getName());
                        if (clientInfo == null || !PLAYER_INPUT_CACHE.containsKey(player))
                            return;

                        boolean cancelsBeforeHand = false;

                        String rawPlayerInput = getPlayerInput(player);
                        int spaces = rawPlayerInput.contains(" ") ? rawPlayerInput.split(" ").length : 0;

                        String playerInput = StringUtils.getFirstArg(rawPlayerInput),
                                server = player.getServer().getInfo().getName();

                        if (!playerInput.equals("/")) {
                            cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(player, StringUtils.replaceFirst(playerInput, "/", ""), server);

                            if (!cancelsBeforeHand)
                                cancelsBeforeHand = !Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", "")) ||  Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", ""));
                        }

                        final String cursor = playerInput;

                        if (cursor.startsWith("/")) {
                            if (spaces == 0) {
                                List<String> suggestions = new ArrayList<>(response.getCommands());

                                if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                                    setPluginCommands();

                                    Storage.Blacklist.getServerBlacklists(server).forEach(blacklist -> blacklist.getCommands().stream().filter(command -> !suggestions.contains("/" + command)).forEach(command -> {
                                        if (BungeePacketAnalyzer.PLUGIN_COMMANDS.contains(command))
                                            suggestions.add("/" + command);
                                    }));

                                    BungeePacketAnalyzer.PLUGIN_COMMANDS.stream().filter(command -> !suggestions.contains(command)).forEach(command -> {
                                        if (command.startsWith("/")) 
                                            command = StringUtils.replaceFirst(command, "/", "");
                                        
                                        if (Storage.Blacklist.canPlayerAccessTab(player, command, server)) {
                                            suggestions.add("/" + command);
                                        }
                                    });

                                    suggestions.stream().filter(suggestion -> suggestion.startsWith(cursor) && !response.getCommands().contains(suggestion)).forEach(command -> response.getCommands().add(command));

                                } else {
                                    BungeePacketAnalyzer.PLUGIN_COMMANDS.stream().filter(command -> !suggestions.contains(command)).forEach(suggestions::add);

                                    suggestions.removeIf(command -> {
                                        if (command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");
                                        return !Storage.Blacklist.canPlayerAccessTab(player, command, server);
                                    });

                                    response.getCommands().clear();
                                    suggestions.forEach(command -> response.getCommands().add(command));
                                }

                                if (response.getCommands().isEmpty()) return;
                            } else {
                                if(cancelsBeforeHand) return;
                            }

                            player.unsafe().sendPacket(new TabCompleteResponse(response.getCommands()));
                        }
                    }
                }
            }

            list.add(wrapper);
        }
    }
}