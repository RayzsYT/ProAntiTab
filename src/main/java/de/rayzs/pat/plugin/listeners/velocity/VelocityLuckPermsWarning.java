package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.LuckPermsWarning;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.concurrent.TimeUnit;

public class VelocityLuckPermsWarning {

    private final ProxyServer server;

    public VelocityLuckPermsWarning(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onCommandExecute(final CommandExecuteEvent event) {
        if (LuckPermsWarning.alreadyAnnounced()) return;

        final CommandSource commandSource = event.getCommandSource();
        if(! (commandSource instanceof Player player)) return;

        final CommandSender sender = CommandSender.from(player);
        final String command = event.getCommand();

        if (LuckPermsWarning.shouldSendLuckPermsWarning(sender, command)) {
            server.getScheduler().buildTask(
                    VelocityLoader.getInstance(),
                    () -> LuckPermsWarning.sendAnnouncement(sender)
            ).delay(1, TimeUnit.SECONDS).schedule();
        }
    }
}
