package de.rayzs.pat.plugin.process.impl.local.system;

import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.Reflection;
import java.util.List;

public class ReloadCommand extends ProCommand {

    public ReloadCommand() {
        super(
                "reload",
                "rl"
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean proxy = Reflection.isProxyServer();
        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !proxy;
        sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.LOADING);

        Storage.loadAll(Reflection.isProxyServer() || !backend);
        CustomServerBrand.initialize();
        GroupManager.clearAllGroups();
        GroupManager.initialize();

        if (!proxy) {
            BackendUpdater.stop();
            BackendUpdater.start();
        }

        ConfigUpdater.broadcastMissingParts();

        if (!backend) {
            Storage.handleChange();
        }

        ConfigUpdater.initialize();

        sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.DONE);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }

}
