package de.rayzs.pat.api.netty.proxy;

import com.mojang.brigadier.arguments.StringArgumentType;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.client.ClientInfo;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.MessageToMessageDecoder;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import de.rayzs.pat.api.communication.Communicator;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import com.mojang.brigadier.tree.CommandNode;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.protocol.packet.*;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.ProxyServer;
import com.mojang.brigadier.builder.*;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;
import io.netty.channel.*;
import java.util.*;

public class BungeePacketAnalyzer {

    public static final ConcurrentHashMap<ProxiedPlayer, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String HANDLER_NAME = "pat-bungee-handler", PIPELINE_NAME = "packet-decoder";
    private static final HashMap<ProxiedPlayer, Boolean> PLAYER_MODIFIED = new HashMap<>();
    private static final HashMap<ProxiedPlayer, String> PLAYER_INPUT_CACHE = new HashMap<>();

    private static final com.mojang.brigadier.Command DUMMY_COMMAND = (context) -> 0;
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
                System.err.println("Failed to inject " + player.getName() + "! Channel is null.");
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

    public static void insertPlayerInput(ProxiedPlayer player, String text) {
        PLAYER_INPUT_CACHE.put(player, text.toLowerCase());
    }

    private static void modifyCommands(ProxiedPlayer player, Commands commands, List<String> list) {
        final String serverName = player.getServer().getInfo().getName();
        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        list.clear();

        for (Map.Entry<String, Command> command : ProxyServer.getInstance().getPluginManager().getCommands()) {
            if (!ProxyServer.getInstance().getDisabledCommands().contains(command.getKey()) && commands.getRoot().getChild(command.getKey()) == null && command.getValue().hasPermission(player)) {

                List<String> commandsToCheck =
                        new ArrayList<>(Arrays.asList(command.getValue().getAliases()));
                if(!commandsToCheck.contains(command.getValue().getName()))
                    commandsToCheck.add(command.getValue().getName());
                if(!commandsToCheck.contains(command.getKey()))
                    commandsToCheck.add(command.getKey());

                commandsToCheck.removeAll(list);
                commandsToCheck.removeIf(commandName -> commandName.startsWith("/"));

                for (String commandName : commandsToCheck) {
                    if (!list.contains(commandName)) {
                        if (!Storage.Blacklist.isBlocked(player, commandName, !turn, serverName)) {
                            list.add(commandName);
                            CommandNode dummy = LiteralArgumentBuilder.literal(commandName)
                                    .executes(DUMMY_COMMAND)
                                    .then(RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString())
                                            .suggests(Commands.SuggestionRegistry.ASK_SERVER).executes(DUMMY_COMMAND))
                                    .build();

                            commands.getRoot().addChild(dummy);
                        }
                    }
                }
            }
        }
    }

    private static class PacketDecoder extends MessageToMessageDecoder<PacketWrapper> {

        private final ProxiedPlayer player;
        private final List<String> commands = new ArrayList<>();

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
                modifyCommands(player, response, commands);
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
                        String playerInput = getPlayerInput(player), server = player.getServer().getInfo().getName();
                        int spaces = 0;

                        if(playerInput.contains(" ")) {
                            String[] split = playerInput.split(" ");
                            spaces = split.length;
                            if(spaces > 0) playerInput = split[0];
                        }

                        if(!playerInput.equals("/")) {
                            cancelsBeforeHand = Storage.Blacklist.isBlocked(player, StringUtils.replaceFirst(playerInput, "/", ""), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server);
                            if(!cancelsBeforeHand) cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(StringUtils.replaceFirst(playerInput, "/", "")) ||  Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(StringUtils.replaceFirst(playerInput, "/", ""));
                        }
                        final String cursor = playerInput;
                        if(cursor.startsWith("/")) {
                            if(spaces == 0) {
                                List<String> suggestions = new ArrayList<>(response.getCommands());

                                if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                                    setPluginCommands();

                                    Storage.Blacklist.getAllBlacklists(server).forEach(blacklist -> blacklist.getCommands().stream().filter(command -> !suggestions.contains("/" + command)).forEach(command -> {
                                        if (BungeePacketAnalyzer.PLUGIN_COMMANDS.contains(command))
                                            suggestions.add("/" + command);
                                    }));

                                    BungeePacketAnalyzer.PLUGIN_COMMANDS.stream().filter(command -> !suggestions.contains(command)).forEach(command -> {
                                        if(command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");
                                        if(PermissionUtil.hasBypassPermission(player, command, server)) {
                                            suggestions.add("/" + command);
                                        }
                                    });

                                    suggestions.stream().filter(suggestion -> suggestion.startsWith(cursor) && !response.getCommands().contains(suggestion)).forEach(command -> response.getCommands().add(command));

                                } else {
                                    BungeePacketAnalyzer.PLUGIN_COMMANDS.stream().filter(command -> !suggestions.contains(command)).forEach(suggestions::add);
                                    suggestions.removeIf(command -> {
                                        if(command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");
                                        return Storage.Blacklist.isBlocked(player, command, true, server, true);
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