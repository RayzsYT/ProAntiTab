package de.rayzs.pat.plugin.system.serverbrand.impl;

import de.rayzs.pat.plugin.system.serverbrand.ServerBrand;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.api.connection.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.utils.PacketUtils;
import java.util.concurrent.TimeUnit;

public class BungeeServerBrand implements ServerBrand {

    private static final ProxyServer SERVER = BungeeLoader.getPlugin().getProxy();
    private static ScheduledTask TASK;
    private static String BRAND = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0);

    public BungeeServerBrand() { }

    @Override
    public void initializeTask() {
        if(TASK != null) {
            TASK.cancel();
        }

        if (!Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            BRAND = MessageTranslator.replaceMessage(
                    Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(0)
            ) + "§r";

            SERVER.getPlayers().forEach(this::send);
            return;
        }

        TASK = SERVER.getScheduler().schedule(BungeeLoader.getPlugin(), new Runnable() {

            final int length = Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().size();
            int animationPos = 0;

            @Override
            public void run() {
                animationPos = (++animationPos) % length;

                BRAND = MessageTranslator.replaceMessage(
                        Storage.ConfigSections.Settings.CUSTOM_BRAND.BRANDS.getLines().get(animationPos)
                ) + "§r";

                SERVER.getPlayers().forEach(player -> send(player));
            }

        }, 1, Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void preparePlayer(Object playerObj) { }

    @Override
    public void send(Object playerObj) {
        if (!(playerObj instanceof ProxiedPlayer player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return;
        }

        final PacketUtils.BrandManipulate serverBrand = createPacket(playerObj);
        final String brand = player.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_13
                ? "minecraft:brand" : "MC|Brand";

        player.sendData(brand, serverBrand.getBytes());
    }

    @Override
    public PacketUtils.BrandManipulate createPacket(Object playerObj) {
        if (!(playerObj instanceof ProxiedPlayer player) || !Storage.ConfigSections.Settings.CUSTOM_BRAND.ENABLED) {
            return null;
        }

        final Server server = player.getServer();

        final String playerName = player.getName();
        final String serverName = server != null ? server.getInfo().getName() : "";

        final String customBrand = StringUtils.replace(BRAND,
                "%player%", playerName,
                "%server%", serverName
        );

        return new PacketUtils.BrandManipulate(customBrand);
    }
}
