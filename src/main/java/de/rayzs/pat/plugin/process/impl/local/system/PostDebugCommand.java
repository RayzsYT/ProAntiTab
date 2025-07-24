package de.rayzs.pat.plugin.process.impl.local.system;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.api.storage.Storage;
import java.util.*;

public class PostDebugCommand extends ProCommand {

    public PostDebugCommand() {
        super(
                "postdebug",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        try {
            sender.sendMessage(Storage.ConfigSections.Messages.POST_DEBUG.SUCCESS.replace("%link%", Objects.requireNonNull(Logger.post())));
        } catch (Exception exception) {
            sender.sendMessage(Storage.ConfigSections.Messages.POST_DEBUG.FAILED);
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
