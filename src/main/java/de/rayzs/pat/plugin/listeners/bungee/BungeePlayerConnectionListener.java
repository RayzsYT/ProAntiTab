package de.rayzs.pat.plugin.listeners.bungee;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Commands;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
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
                                            ProxyServer.getInstance().getScheduler().runAsync(BungeeLoader.getPlugin(), () -> {
                                                RootCommandNode rootCommandNode = new RootCommandNode();
                                                player.getPendingConnection().unsafe().sendPacket(new Commands(rootCommandNode));
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
