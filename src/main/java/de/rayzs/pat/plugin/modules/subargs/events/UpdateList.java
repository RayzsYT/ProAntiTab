package de.rayzs.pat.plugin.modules.subargs.events;

import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.subargs.Arguments;

import java.util.*;

public class UpdateList {

    public static UpdatePluginEvent UPDATE_PLUGIN_EVENT = new UpdatePluginEvent() {
        @Override
        public void handle(UpdatePluginEvent event) {
            SubArgsModule.updateMessages();
            SubArgsModule.updateList();
        }
    };

    public static ServerPlayersChangeEvent SERVER_PLAYERS_CHANGE_EVENT = new ServerPlayersChangeEvent() {
        @Override
        public void handle(ServerPlayersChangeEvent event) {
            SubArgsModule.updatePlayerNames();
        }
    };

    public static ReceiveSyncEvent RECEIVE_SYNC_EVENT = new ReceiveSyncEvent() {
        @Override
        public void handle(ReceiveSyncEvent event) {
            SubArgsModule.updateList();
        }
    };

    public static UpdatePlayerCommandsEvent UPDATE_PLAYER_COMMANDS_EVENT = new UpdatePlayerCommandsEvent() {
        @Override
        public void handle(UpdatePlayerCommandsEvent event) {
            final UUID uuid = event.getSenderObj() instanceof UUID
                    ? (UUID) event.getSenderObj()
                    : new CommandSender(event.getSenderObj()).getUniqueId();

            final Arguments argument = SubArgsModule.PLAYER_COMMANDS.getOrDefault(uuid, new Arguments());
            final String serverName = Storage.getLoader().getPlayerServerName(uuid);

            argument.clearArguments();

            if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                for (String command : event.getCommands())
                    argument.buildArgumentStacks(command);

                for (String command : argument.CHAT_ARGUMENTS.getGeneralArgument().getInputs())
                    argument.CHAT_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));

                for (String command : argument.TAB_ARGUMENTS.getGeneralArgument().getInputs())
                    argument.TAB_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));

                for (String command : SubArgsModule.getGroupCommands(uuid, serverName))
                    argument.buildArgumentStacks(command);

                for (String command : SubArgsModule.getServerCommands(uuid))
                    argument.buildArgumentStacks(command);

            } else {
                List<String> groupCommands = SubArgsModule.getGroupCommands(uuid, serverName);

                for (String command : argument.CHAT_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    argument.CHAT_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }

                for (String command : argument.TAB_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    argument.TAB_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }
            }

            SubArgsModule.PLAYER_COMMANDS.putIfAbsent(uuid, argument);
        }
    };
}
