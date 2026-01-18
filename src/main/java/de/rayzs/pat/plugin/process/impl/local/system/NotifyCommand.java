package de.rayzs.pat.plugin.process.impl.local.system;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.Reflection;

import java.util.List;

public class NotifyCommand extends ProCommand {

    public NotifyCommand() {
        super(
                "notify",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean enabled;

        if (sender.isConsole()) {
            enabled = Storage.SEND_CONSOLE_NOTIFICATION;
            Storage.SEND_CONSOLE_NOTIFICATION = !Storage.SEND_CONSOLE_NOTIFICATION;
        } else {

            boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !Reflection.isProxyServer();

            if (backend) {
                sender.sendMessage(Storage.ConfigSections.Messages.NO_PROXY.MESSAGE);
                return true;
            }

            enabled = Storage.NOTIFY_PLAYERS.contains(sender.getUniqueId());

            if (enabled)
                Storage.NOTIFY_PLAYERS.remove(sender.getUniqueId());
            else
                Storage.NOTIFY_PLAYERS.add(sender.getUniqueId());
        }

        String message = enabled
                ? Storage.ConfigSections.Messages.NOTIFICATION.DISABLED
                : Storage.ConfigSections.Messages.NOTIFICATION.ENABLED;

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
