package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VelocityPingListener {

    private static ProxyServer server;
    private static final UUID RANDOM_UUID = UUID.randomUUID();

    public VelocityPingListener(ProxyServer server) {
        VelocityPingListener.server = server;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;

        ServerPing oldPing = event.getPing();
        ServerPing.Builder builder = oldPing.asBuilder();

        int online = server.getPlayerCount(),
                max = server.getConfiguration().getShowMaxPlayers(),
                protocol = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW ? -1 : oldPing.getVersion().getProtocol(),
                extend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT;


        builder.version(new ServerPing.Version(protocol,
                replaceString(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL, online, extend, max))
        );

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_EXTEND_AS_MAX_COUNT)
            builder.maximumPlayers(extend);

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.HIDE_PLAYERS)
            builder.clearSamplePlayers();
        else if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_CUSTOM_PLAYERLIST) {
            builder.clearSamplePlayers();

            if (!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_CENTER_VARIABLE) {

                Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PLAYERLIST.getLines().forEach(line ->
                        builder.samplePlayers(new ServerPing.SamplePlayer(replaceString(line, online, extend, max), RANDOM_UUID))
                );

            } else {
                List<String> cpyLines = new ArrayList<>(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PLAYERLIST.getLines());

                cpyLines.replaceAll(string -> replaceString(string, online, extend, max));
                StringUtils.centralize(cpyLines);

                for (String line : cpyLines) {
                    builder.samplePlayers(new ServerPing.SamplePlayer(line, RANDOM_UUID));
                }

            }
        }

        event.setPing(builder.build());
    }


    private String replaceString(String string, int online, int onlineExtend, int max) {
        return string
                .replace("&", "§")
                .replace("%online_extended%", String.valueOf(onlineExtend))
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max));
    }
}
