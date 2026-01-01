package de.rayzs.pat.plugin.subarguments.events;

import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.subargs.Arguments;

import java.util.*;

public class UpdateList {

    public static UpdatePluginEvent UPDATE_PLUGIN_EVENT = new UpdatePluginEvent() {
        @Override
        public void handle(UpdatePluginEvent event) {
            SubArguments.updateMessages();
            SubArguments.updateList();
        }
    };

    public static ServerPlayersChangeEvent SERVER_PLAYERS_CHANGE_EVENT = new ServerPlayersChangeEvent() {
        @Override
        public void handle(ServerPlayersChangeEvent event) {
            SubArguments.updatePlayerNames();
        }
    };

    public static ReceiveSyncEvent RECEIVE_SYNC_EVENT = new ReceiveSyncEvent() {
        @Override
        public void handle(ReceiveSyncEvent event) {
            SubArguments.updateList();
        }
    };

    public static UpdatePlayerCommandsEvent UPDATE_PLAYER_COMMANDS_EVENT = new UpdatePlayerCommandsEvent() {
        @Override
        public void handle(UpdatePlayerCommandsEvent event) {
            final UUID uuid = event.getSenderObj() instanceof UUID
                    ? (UUID) event.getSenderObj()
                    : CommandSenderHandler.from(event.getSenderObj()).getUniqueId();

            final Arguments argument = SubArguments.PLAYER_COMMANDS.getOrDefault(uuid, new Arguments());
            final String serverName = Storage.getLoader().getPlayerServerName(uuid);

            argument.clearArguments();

            if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                for (String command : event.getCommands())
                    argument.buildArgumentStacks(command);

                final List<String> chatInputs = new ArrayList<>(argument.CHAT_ARGUMENTS.getGeneralArgument().getInputs());
                final List<String> tabInput = new ArrayList<>(argument.TAB_ARGUMENTS.getGeneralArgument().getInputs());


                for (String command : chatInputs) {
                    Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);

                    if (type != Storage.Blacklist.BlockType.NEGATE) {
                        command = Storage.Blacklist.BlockTypeFetcher.modify(command, type);
                    }

                    argument.CHAT_ARGUMENTS.buildArguments(command);
                }

                for (String command : tabInput) {
                    Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);


                    if (type != Storage.Blacklist.BlockType.NEGATE)
                        command = Storage.Blacklist.BlockTypeFetcher.modify(command, type);

                    argument.TAB_ARGUMENTS.buildArguments(command);
                }

                for (String command : SubArguments.getGroupCommands(uuid, serverName))
                    argument.buildArgumentStacks(command);

                for (String command : SubArguments.getServerCommands(uuid)) {
                    argument.buildArgumentStacks(command);
                }

            } else {
                List<String> groupCommands = SubArguments.getGroupCommands(uuid, serverName);

                for (String command : argument.CHAT_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    argument.CHAT_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }

                for (String command : argument.TAB_ARGUMENTS.getGeneralArgument().getInputs()) {
                    if (groupCommands.contains(command)) continue;

                    argument.TAB_ARGUMENTS.buildArguments(Storage.Blacklist.BlockTypeFetcher.modify(command));
                }
            }

            SubArguments.PLAYER_COMMANDS.putIfAbsent(uuid, argument);
        }
    };
}
