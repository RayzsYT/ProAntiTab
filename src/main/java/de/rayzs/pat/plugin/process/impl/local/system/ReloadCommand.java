package de.rayzs.pat.plugin.process.impl.local.system;

import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
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
        sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.LOADING);
        Storage.reload();
        sender.sendMessage(Storage.ConfigSections.Messages.RELOAD.DONE);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }

}
