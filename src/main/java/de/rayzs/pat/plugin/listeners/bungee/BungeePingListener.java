package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.*;
import net.md_5.bungee.event.*;
import net.md_5.bungee.api.*;

import java.util.ArrayList;
import java.util.List;

public class BungeePingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyPing(ProxyPingEvent event) {
        if(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;

        ProxyServer proxyServer = BungeeLoader.getPlugin().getProxy();
        int online = proxyServer.getOnlineCount(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                tmpMax = -1;

        try {
            tmpMax = proxyServer.getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
        } catch (Throwable throwable) {
            Logger.warning("Failed to read max-players count for %max% placeholder! Using -1 as default value instead.");
        }

        final int max = tmpMax;

        ServerPing serverPing = event.getResponse();

        int protocol = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW ? -1 : serverPing.getVersion().getProtocol();
        String version = replaceString(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL, online, onlineExtend, max);

        ServerPing.Protocol newProtocol = new ServerPing.Protocol(version, protocol);
        serverPing.setVersion(newProtocol);

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_EXTEND_AS_MAX_COUNT)
            serverPing.getPlayers().setMax(onlineExtend);

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.HIDE_PLAYERS)
            serverPing.getPlayers().setSample(new ServerPing.PlayerInfo[0]);
        else if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_CUSTOM_PLAYERLIST) {
            List<String> lines = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PLAYERLIST.getLines();
            ServerPing.PlayerInfo[] playerInfos = new ServerPing.PlayerInfo[lines.size()];

            if (!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_CENTER_VARIABLE) {

                for (int i = 0; i < lines.size(); i++) {
                    String line = replaceString(lines.get(i), online, onlineExtend, max);
                    playerInfos[i] = new ServerPing.PlayerInfo(line, "");
                }

            } else {
                List<String> cpyLines = new ArrayList<>(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PLAYERLIST.getLines());

                cpyLines.replaceAll(string -> replaceString(string, online, onlineExtend, max));
                StringUtils.centralize(cpyLines);

                for (int i = 0; i < cpyLines.size(); i++) {
                    playerInfos[i] = new ServerPing.PlayerInfo(cpyLines.get(i), "");
                }
            }

            serverPing.getPlayers().setSample(playerInfos);
        }

        event.setResponse(serverPing);
    }

    private String replaceString(String string, int online, int onlineExtend, int max) {
        return string
                .replace("&", "§")
                .replace("%online_extended%", String.valueOf(onlineExtend))
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max));
    }
}
