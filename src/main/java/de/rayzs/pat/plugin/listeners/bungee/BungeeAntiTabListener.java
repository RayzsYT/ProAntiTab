package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;

public class BungeeAntiTabListener implements Listener {

    @EventHandler
    public void onTabCompleteResponse(TabCompleteResponseEvent event) {

        if(!(event.getReceiver() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();

        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo.getName();

        String rawCursor = BungeePacketAnalyzer.getPlayerInput(player);

        if (rawCursor == null) {
            return;
        }

        if (!rawCursor.startsWith("/")) {
            return;
        }

        String cursor = rawCursor.substring(1);

        if (event.getSuggestions().isEmpty())
            return;

        if (Storage.Blacklist.isDisabledServer(serverName))
            return;

        if (PermissionUtil.hasBypassPermission(player))
            return;



        final boolean doesBypassNamespace = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player);
        final boolean spaces = cursor.contains(" ");

        boolean cancelsBeforeHand = false;

        if (Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(cursor) && !doesBypassNamespace) {
            cancelsBeforeHand = true;
        }

        if (!cancelsBeforeHand && !cursor.isEmpty()) {
            cancelsBeforeHand = !Storage.Blacklist.canPlayerAccessTab(player, StringUtils.getFirstArg(cursor), serverName);
        }

        if (!cancelsBeforeHand) {
            cancelsBeforeHand = Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(cursor) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(cursor);
        }

        if (spaces) {
            if (cancelsBeforeHand) {
                event.getSuggestions().clear();
                return;
            }

            FilteredTabCompletionEvent filteredTabCompletionEvent = PATEventHandler.callFilteredTabCompletionEvents(player.getUniqueId(), rawCursor, new ArrayList<>(event.getSuggestions()));

            if (filteredTabCompletionEvent.isCancelled()) {
                event.getSuggestions().clear();
            } else {
                event.getSuggestions().removeIf(s -> !filteredTabCompletionEvent.getCompletion().contains(s));
            }

            return;
        }

        event.getSuggestions().removeIf(s -> {
            String cpy = s;
            if (cpy.startsWith("/")) {
                cpy = cpy.substring(1);
            }

            if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(cpy) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(cpy)) {
                return false;
            }

            return !Storage.Blacklist.canPlayerAccessTab(player, cpy, serverName);
        });
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        BungeePacketAnalyzer.setPlayerInput(player, event.getCursor());
    }
}
