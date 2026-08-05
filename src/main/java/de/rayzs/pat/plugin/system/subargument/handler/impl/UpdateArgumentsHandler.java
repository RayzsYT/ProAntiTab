package de.rayzs.pat.plugin.system.subargument.handler.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.subargument.handler.SubArgumentHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.plugin.system.subargument.argument.Arguments;

import java.util.List;

public class UpdateArgumentsHandler extends SubArgumentHandler {

    public UpdateArgumentsHandler(SubArgument instance) {
        super(instance);
    }

    public void updatePlayerArguments(CommandSender sender, List<String> commands, List<String> serverCommands, List<String> groupCommands) {
        final Arguments arguments = getInstance().getPlayerArgument(sender);

        arguments.clearArguments();

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            for (String command : commands) {
                arguments.buildArgumentStacks(command);
            }

            for (String groupCommand : groupCommands) {
                arguments.buildArgumentStacks(groupCommand);
            }

        } else {
            if (Reflection.isProxyServer()) {
                serverCommands.removeIf(groupCommands::contains);

                for (String command : serverCommands) {
                    arguments.buildArgumentStacks(command);
                }

            } else {

                for (String command : Arguments.get().CHAT_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    arguments.CHAT_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }

                for (String command : Arguments.get().TAB_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    arguments.TAB_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }
            }

        }

    }
}
