package de.rayzs.pat.plugin.system.serverbrand.impl;

import de.rayzs.pat.plugin.packetanalyzer.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.plugin.system.serverbrand.ServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.scheduler.PATSchedulerTask;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import de.rayzs.pat.utils.*;
import java.lang.reflect.*;
import org.bukkit.*;

import java.util.*;

public class BukkitServerBrand implements ServerBrand {

    private static final Server SERVER = Bukkit.getServer();

    private static final String CHANNEL_NAME = Reflection.isBefore(1, 13) ? "MC|Brand" : "minecraft:brand";

    private static String BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0);
    private static PATSchedulerTask TASK;

    private static boolean INITIALIZED = false,
                           NO_PREP_CHANNELS = false;

    private static Class<?> brandPayloadClass, clientBoundCustomPacketPayloadPacketClass, customPacketPayloadPacketClass;

    @Override
    public void initializeTask() {

        if (Reflection.isWeird()) {

            NO_PREP_CHANNELS = Reflection.isAtLeast(1, 21, 7);;

            if (brandPayloadClass == null)
                brandPayloadClass = Reflection.getClass("net.minecraft.network.protocol.common.custom.BrandPayload");

            if (clientBoundCustomPacketPayloadPacketClass == null)
                clientBoundCustomPacketPayloadPacketClass = Reflection.getClass("net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket");

            if (customPacketPayloadPacketClass == null)
                customPacketPayloadPacketClass = Reflection.getClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
        }

        Bukkit.getOnlinePlayers().forEach(this::preparePlayer);

        if (!INITIALIZED) {
            try {
                Method method = Reflection.getMethodsByParameterAndName(SERVER.getMessenger(), "addToOutgoing", Plugin.class, String.class).get(0);
                Reflection.invokeMethode(method, SERVER.getMessenger(), BukkitLoader.getPlugin(), CHANNEL_NAME);
                SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
                INITIALIZED = true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        if (TASK != null && TASK.isActive()) {
            TASK.cancelTask();
            TASK = null;
        }

        if (!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0) + "§r";

            PATScheduler.createAsyncScheduler(() -> {
                Bukkit.getOnlinePlayers().forEach(this::send);
            }, 1, 1);

        } else {

            TASK = PATScheduler.createAsyncScheduler(new Runnable() {

                final int length = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size();
                int animationPos = 0;

                @Override
                public void run() {
                    animationPos = (++animationPos) % length;

                    BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationPos) + "§r";
                    Bukkit.getOnlinePlayers().forEach(player -> send(player));
                }

            }, 1, Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY);
        }
    }

    @Override
    public void preparePlayer(Object playerObj) {
        if (!(playerObj instanceof Player player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        if (NO_PREP_CHANNELS) {
            return;
        }


        try {
            final Field channelsField = Reflection.getFieldByName(player.getClass(), "channels");
            final Set<String> channels = (Set<String>) channelsField.get(player);

            channels.add(CHANNEL_NAME);
            Reflection.closeAccess(channelsField);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void send(Object playerObj) {
        if(! (playerObj instanceof Player player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        final String playerName = player.getName(), displayName = player.getDisplayName(), worldName = player.getWorld().getName();
        final String customBrand = StringUtils.replace(BRAND,
                "%player%", playerName,
                "%displayname%", displayName,
                "%world%", worldName
        );

        if (!Reflection.isWeird()) {
            final PacketUtils.BrandManipulate serverBrand = new PacketUtils.BrandManipulate(
                    MessageTranslator.replaceMessage(player, customBrand)
            );

            player.sendPluginMessage(
                    BukkitLoader.getPlugin(),
                    CHANNEL_NAME,
                    serverBrand.getBytes()
            );

            return;
        }

        try {
            final Channel channel = BukkitPacketAnalyzer.INJECTED_PLAYERS.get(player.getUniqueId());

            if (channel == null) {
                return;
            }

            final Object brandPayloadObj = brandPayloadClass
                    .getDeclaredConstructor(String.class)
                    .newInstance(MessageTranslator.replaceMessage(player, customBrand));

            final Object customPacketPayloadPacket = clientBoundCustomPacketPayloadPacketClass
                    .getDeclaredConstructor(customPacketPayloadPacketClass)
                    .newInstance(brandPayloadObj);

            channel.pipeline().writeAndFlush(customPacketPayloadPacket);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public PacketUtils.BrandManipulate createPacket(Object playerObj) {
        return null;
    }
}
