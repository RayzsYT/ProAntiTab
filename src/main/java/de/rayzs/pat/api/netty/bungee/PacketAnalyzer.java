package de.rayzs.pat.api.netty.bungee;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.Reflection;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Commands;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketAnalyzer {

    public static final ConcurrentHashMap<ProxiedPlayer, Channel> INJECTED_PLAYERS = new ConcurrentHashMap<>();

    private static final String PIPELINE_NAME = "pat-bungee-packethandler", HANDLER_NAME = "packet-decoder";
    private static final HashMap<ProxiedPlayer, Boolean> PLAYER_MODIFIED = new HashMap<>();

    private static final com.mojang.brigadier.Command DUMMY_COMMAND = (context) -> 0;

    private static Class<?> channelWrapperClass, serverConnectionClass;

    public static void uninjectAll() {
        PacketAnalyzer.INJECTED_PLAYERS.keySet().forEach(PacketAnalyzer::uninject);
        PacketAnalyzer.INJECTED_PLAYERS.clear();
    }

    public static boolean inject(ProxiedPlayer player) {

        if(channelWrapperClass == null)
            channelWrapperClass = Reflection.getClass("net.md_5.bungee.netty.ChannelWrapper");

        if(serverConnectionClass == null)
            serverConnectionClass = Reflection.getClass("net.md_5.bungee.ServerConnection");

        Object channelWrapperObj;
        Field channelField = null;
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
            if(channel.pipeline().names().contains(PacketAnalyzer.HANDLER_NAME))
                uninject(player);

            channel.pipeline().addBefore(PacketAnalyzer.HANDLER_NAME, PacketAnalyzer.PIPELINE_NAME, new PacketDecoder(player));
            PacketAnalyzer.INJECTED_PLAYERS.put(player, channel);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        } return true;
    }

    public static boolean isPlayerModified(ProxiedPlayer player) {
        PLAYER_MODIFIED.putIfAbsent(player, false);
        return PLAYER_MODIFIED.get(player);
    }

    public static void setPlayerModification(ProxiedPlayer player, boolean modified) {
        PLAYER_MODIFIED.put(player, modified);
    }

    public static void uninject(ProxiedPlayer player) {
        PLAYER_MODIFIED.remove(player);

        if(PacketAnalyzer.INJECTED_PLAYERS.containsKey(player)) {
            Channel channel = PacketAnalyzer.INJECTED_PLAYERS.get(player);
            if(channel != null) {
                PacketAnalyzer.INJECTED_PLAYERS.remove(player);
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (pipeline.names().contains(PacketAnalyzer.PIPELINE_NAME)) pipeline.remove(PacketAnalyzer.PIPELINE_NAME);
                });
            }
        }
    }

    private static void modifyCommands(ProxiedPlayer player, Commands commands) {
        for (Map.Entry<String, Command> command : ProxyServer.getInstance().getPluginManager().getCommands()) {
            if (!ProxyServer.getInstance().getDisabledCommands().contains(command.getKey()) && commands.getRoot().getChild(command.getKey()) == null && command.getValue().hasPermission(player)) {
                if (Storage.Blacklist.isBlocked(player, command.getKey(), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED))
                    continue;

                CommandNode dummy = LiteralArgumentBuilder.literal(command.getKey())
                        .executes(DUMMY_COMMAND)
                        .then(RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString())
                        .suggests(Commands.SuggestionRegistry.ASK_SERVER).executes(DUMMY_COMMAND))
                        .build();

                commands.getRoot().addChild(dummy);
            }
        }
    }

    private static class PacketDecoder extends MessageToMessageDecoder<PacketWrapper> {

        private final ProxiedPlayer player;

        private PacketDecoder(ProxiedPlayer player) {
            this.player = player;
        }

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, PacketWrapper wrapper, List<Object> list) throws Exception {
            if (wrapper.packet == null) {
                list.add(wrapper);
                return;
            }

            if (wrapper.packet instanceof Commands) {
                if(isPlayerModified(player)) {
                    System.out.println("Cancelled because already set!");
                    return;
                }

                setPlayerModification(player, true);

                Commands response = (Commands) wrapper.packet;
                System.out.println("Received packet");

                modifyCommands(player, response);
                System.out.println("Manipulating packet");

                player.getPendingConnection().unsafe().sendPacket(response);
                player.sendMessage("Send packet");

                return;
            }

            list.add(wrapper);
        }
    }
}
