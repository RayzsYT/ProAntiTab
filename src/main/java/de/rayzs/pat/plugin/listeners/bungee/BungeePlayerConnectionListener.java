package de.rayzs.pat.plugin.listeners.bungee;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import de.rayzs.pat.api.brand.impl.BungeeServerBrand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Commands;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BungeePlayerConnectionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PermissionUtil.setPlayerPermissions(player.getUniqueId());

        if(Storage.OUTDATED && (PermissionUtil.hasPermission(player, "update"))) {
            ProxyServer.getInstance().getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
                if (player.isConnected()) {
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED, "%player%", player.getName());
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        BungeeServerBrand.removeFromModified(player);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PermissionUtil.resetPermissions(player.getUniqueId());
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        BungeeServerBrand.removeFromModified(player);
    }


    /* EXPERIMENTAL CODE:
        Don't worry... it won't look like it when it's actually done. ;3
        The actual code is gonna be on this path later on:
        => de.rayzs.pat.api.netty.proxy
     */

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Connection.Unsafe unsafe = player.getPendingConnection().unsafe();

        Class<?> channelWrapperClass = Reflection.getClass("net.md_5.bungee.netty.ChannelWrapper"),
                serverConnectionClass = Reflection.getClass("net.md_5.bungee.ServerConnection");
        Object channelWrapperObj, serverConnectionObj;
        Field channelField = null;
        Channel channel;

        serverConnectionObj = event.getServer();

        try {
            channelField = Reflection.getFieldByName(serverConnectionClass, "ch");
            channelField.setAccessible(true);
            channelWrapperObj = channelField.get(serverConnectionObj);
            channel = (Channel) Reflection.getFieldsByType(channelWrapperClass, "Channel", Reflection.SearchOption.ENDS).get(0).get(channelWrapperObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (channelField != null) {
                channelField.setAccessible(false);
            }
        }

        try {
            System.out.println("Trying inject into commandMultimap");
            Multimap<Plugin, Command> commandMultimap = (Multimap<Plugin, Command>) Reflection.getFieldByName(ProxyServer.getInstance().getPluginManager().getClass(), "commandsByPlugin").get(ProxyServer.getInstance().getPluginManager());
            Map<Plugin, Collection<Command>> map = commandMultimap.asMap();
            System.out.println("Maybe successful? " + map.size());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        ChannelPipeline pipeline = channel.pipeline();

            pipeline.addBefore("inbound-boss", "pat-test", new ChannelDuplexHandler() {
                        @Override
                        public void channelRead(ChannelHandlerContext channel, Object msg) {
                            try {
                                if (msg != null) {
                                    PacketWrapper packetWrapper = (PacketWrapper) msg;
                                    ByteBuf content = packetWrapper.buf;
                                    DefinedPacket packet = packetWrapper.packet;

                                    if (packet != null) {
                                        if (packet instanceof Commands) {
                                            Commands response = (Commands) packet;
                                            System.out.println("COMMANS PACKET");

                                            com.mojang.brigadier.Command DUMMY_COMMAND = (context) ->
                                            {
                                                return 0;
                                            };

                                            ProxyServer.getInstance().getScheduler().runAsync(BungeeLoader.getPlugin(), () -> {

                                                Commands commands = new Commands(new RootCommandNode());

                                                for ( Map.Entry<String, Command> command : ProxyServer.getInstance().getPluginManager().getCommands() )
                                                {
                                                    if ( !ProxyServer.getInstance().getDisabledCommands().contains( command.getKey() ) && commands.getRoot().getChild( command.getKey() ) == null && command.getValue().hasPermission( player ) )
                                                    {
                                                        if(!command.getKey().equals("server")) continue;
                                                        CommandNode dummy = LiteralArgumentBuilder.literal( command.getKey() ).executes( DUMMY_COMMAND )
                                                                .then( RequiredArgumentBuilder.argument( "args", StringArgumentType.greedyString() )
                                                                        .suggests( Commands.SuggestionRegistry.ASK_SERVER ).executes( DUMMY_COMMAND ) )
                                                                .build();

                                                        commands.getRoot().addChild( dummy );
                                                    }
                                                }

                                                player.getPendingConnection().unsafe().sendPacket(commands);
                                                player.sendMessage("Send packet");
                                            });
                                        }
                                    }
                                }
                                super.channelRead(channel, msg);
                            } catch (Throwable exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
    }
}
