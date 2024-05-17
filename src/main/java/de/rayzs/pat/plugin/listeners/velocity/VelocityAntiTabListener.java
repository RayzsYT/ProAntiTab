package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;

public class VelocityAntiTabListener {

    private static ProxyServer server;

    public VelocityAntiTabListener(ProxyServer server) {
        VelocityAntiTabListener.server = server;
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        Player player = event.getPlayer();

        if(PermissionUtil.hasBypassPermission(player)) return;

        event.getSuggestions().removeIf(command -> Storage.hasNoAccess(player, command));
    }

    @Subscribe (order = PostOrder.FIRST)
    public void onPlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();

        if(PermissionUtil.hasBypassPermission(player)) return;

        event.getRootNode().getChildren().removeIf(command -> Storage.hasNoAccess(player, command.getName()));
    }

    public static void updateCommands() {
        /* Different solution required
        for (Player player : server.getAllPlayers()) {
            RootCommandNode<?> rootCommandNode = PLAYER_ROOT_NODES.get(player);
            if(rootCommandNode == null) return;

            PlayerAvailableCommandsEvent event = new PlayerAvailableCommandsEvent(player, rootCommandNode);
            server.getEventManager().fireAndForget(event);
        }
         */
    }
}
