package de.rayzs.pat.plugin.command.commands.local.system;

import de.rayzs.pat.plugin.command.commands.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;

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
