package de.rayzs.pat.api.netty.proxy;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.mojang.brigadier.arguments.ArgumentType;
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
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
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
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;

public class BungeePacketAnalyzer {

    public static final ConcurrentHashMap<ProxiedPlayer, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String HANDLER_NAME = "pat-bungee-handler", PIPELINE_NAME = "packet-decoder";
    private static final HashMap<ProxiedPlayer, Boolean> PLAYER_MODIFIED = new HashMap<>();
    private static final ExpireCache<UUID, String> PLAYER_INPUT_CACHE = new ExpireCache<>(5, TimeUnit.SECONDS);

    private static final List<String> PLUGIN_COMMANDS = new ArrayList<>();

    private static Class<?> channelWrapperClass, serverConnectionClass;

    private static List<String> PROXY_COMMANDS;

    static {
        CommandNodeHelper.setDefaultSuggestionProvider(Commands.SuggestionRegistry.ASK_SERVER);
        loadProxyCommands();
    }

    public static void loadProxyCommands() {
        PROXY_COMMANDS = ProxyServer.getInstance().getPluginManager().getCommands().stream().map(entry -> {
            String key = entry.getKey();
            return key.startsWith("/") ? key.substring(1) : key;
        }).toList();
    }

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

            if (channel.pipeline().names().contains(BungeePacketAnalyzer.HANDLER_NAME))
                uninject(player);

            channel.pipeline().addAfter(BungeePacketAnalyzer.PIPELINE_NAME, BungeePacketAnalyzer.HANDLER_NAME, new PacketDecoder(player));
            BungeePacketAnalyzer.INJECTED_PLAYERS.put(player, channel);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;

        }

        return true;
    }

    public static void uninject(ProxiedPlayer player) {
        BungeePacketAnalyzer.PLAYER_INPUT_CACHE.remove(player.getUniqueId());
        BungeePacketAnalyzer.PLAYER_MODIFIED.remove(player);

        if(BungeePacketAnalyzer.INJECTED_PLAYERS.containsKey(player)) {
            Channel channel = BungeePacketAnalyzer.INJECTED_PLAYERS.get(player);
            if(channel != null) {
                BungeePacketAnalyzer.INJECTED_PLAYERS.remove(player);
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();

                    if (pipeline.names().contains(BungeePacketAnalyzer.HANDLER_NAME))
                        pipeline.remove(BungeePacketAnalyzer.HANDLER_NAME);
                });
            }
        }
    }

    public static void setPlayerInput(ProxiedPlayer player, String input) {
        PLAYER_INPUT_CACHE.putIgnoreIfContains(player.getUniqueId(), input);
    }

    public static String getPlayerInput(ProxiedPlayer player) {
        String input = PLAYER_INPUT_CACHE.get(player.getUniqueId());
        PLAYER_INPUT_CACHE.remove(player.getUniqueId());
        return input;
    }

    private static void modifyCommands(ProxiedPlayer player, Commands commands) {
        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo.getName();

        final boolean ignore = PermissionUtil.hasBypassPermission(player) || Storage.Blacklist.isDisabledServer(serverName);

        CommandNodeHelper helper = new CommandNodeHelper<CommandNode>(commands.getRoot());

        List<String> commandsAsString = new ArrayList<>(PROXY_COMMANDS);
        commandsAsString.addAll(helper.getChildrenNames());

        List<String> playerCommands = new ArrayList<>();

        if (!ignore) {

            final List<Group> groups = GroupManager.getPlayerGroups(player);

            final Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();
            if (!cache.containsKey(serverName)) {
                cache.put(serverName, new CommandsCache());
            }

            final CommandsCache commandsCache = cache.get(serverName);
            commandsCache.handleCommands(commandsAsString, serverName);

            final List<String> tmpPlayerCommands = commandsCache.getPlayerCommands(commandsAsString, player, groups, serverName);
            helper.removeIf(str -> !tmpPlayerCommands.contains(str));

            if (Storage.ConfigSections.Settings.CUSTOM_VERSION.ALWAYS_TAB_COMPLETABLE) {
                Storage.ConfigSections.Settings.CUSTOM_VERSION.COMMANDS.getLines().forEach(input -> helper.add(input, false));
            }

            if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.ALWAYS_TAB_COMPLETABLE) {
                Storage.ConfigSections.Settings.CUSTOM_PLUGIN.COMMANDS.getLines().forEach(input -> helper.add(input, false));
            }

            playerCommands = tmpPlayerCommands;
        }

        for (Map.Entry<String, Command> command : ProxyServer.getInstance().getPluginManager().getCommands()) {

            if (ProxyServer.getInstance().getDisabledCommands().contains(command.getKey()))
                continue;

            if (commands.getRoot().getChild(command.getKey()) != null)
                continue;

            if (!command.getValue().hasPermission(player)) {
                continue;
            }

            String commandName = command.getKey();
            commandName = commandName.startsWith("/") ? commandName.substring(1) : commandName;

            if (!ignore) {
                boolean tabablePluginCommand = Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(commandName);
                boolean tabableVersionCommand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(commandName);

                if (!tabablePluginCommand && !tabableVersionCommand && !playerCommands.contains(commandName)) {
                    continue;
                }
            }

            CommandNode dummy = CommandNodeHelper.createDummyCommandNode(command.getKey());
            commands.getRoot().addChild(dummy);
        }


        for (String command : PROXY_COMMANDS) {
            if (ignore || playerCommands.contains(command)) {
                helper.add(command, true);
            }
        }

        if (!ignore) {
            SubArgsModule.handleCommandNode(player.getUniqueId(), helper);
        }
    }

    public static void sendCommandsPacket() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (player.getPendingConnection().getVersion() < 754)
                continue;

            RootCommandNode root = new RootCommandNode();
            Commands packet = new Commands(root);

            modifyCommands(player, packet);
            player.unsafe().sendPacket(packet);
        }
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

            if (wrapper.packet instanceof PluginMessage) {
                PluginMessage pluginMessage = (PluginMessage) wrapper.packet;
                if(CustomServerBrand.isEnabled() && CustomServerBrand.isBrandTag(pluginMessage.getTag()))
                    return;

            } else if (wrapper.packet instanceof Commands) {
                Commands response = (Commands) wrapper.packet;
                modifyCommands(player, response);

                player.unsafe().sendPacket(response);
                return;

                /*
            } else if (wrapper.packet instanceof TabCompleteResponse) {
                TabCompleteResponse response = (TabCompleteResponse) wrapper.packet;
                String serverName = player.getServer().getInfo().getName();

                System.out.println("damn");

                if (!Storage.Blacklist.isDisabledServer(serverName) && player.getPendingConnection().getVersion() < 754) {
                    if (!PermissionUtil.hasBypassPermission(player)) {

                        System.out.println("Further!");
                        System.out.println("Got: " + PLAYER_INPUT_CACHE.get(player.getUniqueId()));

                        if (!PLAYER_INPUT_CACHE.contains(player.getUniqueId()))
                            return;

                        boolean cancelsBeforeHand = false;

                        String rawPlayerInput = PLAYER_INPUT_CACHE.get(player.getUniqueId());
                        int spaces = rawPlayerInput.contains(" ") ? rawPlayerInput.split(" ").length : 0;

                        String playerInput = StringUtils.getFirstArg(rawPlayerInput),
                                server = player.getServer().getInfo().getName();

                        System.out.println("NETTY -> " + playerInput);

                        if (!playerInput.equals("/")) {
                            cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(player, StringUtils.replaceFirst(playerInput, "/", ""), server);

                            if (!cancelsBeforeHand)
                                cancelsBeforeHand = !Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", "")) || Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(StringUtils.replaceFirst(playerInput, "/", ""));
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
                                        if (command.startsWith("/"))
                                            command = StringUtils.replaceFirst(command, "/", "");

                                        return !Storage.Blacklist.canPlayerAccessTab(player, command, server);
                                    });

                                    response.getCommands().clear();
                                    suggestions.forEach(command -> response.getCommands().add(command));
                                }

                                if (response.getCommands().isEmpty())
                                    return;

                            } else {
                                if (cancelsBeforeHand)
                                    return;
                            }

                            player.unsafe().sendPacket(new TabCompleteResponse(response.getCommands()));
                        }
                    }
                }*/
            }

            list.add(wrapper);
        }
    }
}