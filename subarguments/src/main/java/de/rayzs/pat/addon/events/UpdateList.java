package de.rayzs.pat.addon.events;

import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UpdateList {

    public static UpdatePluginEvent UPDATE_PLUGIN_EVENT = new UpdatePluginEvent() {
        @Override
        public void handle(UpdatePluginEvent event) {
            SubArgsAddon.updateMessages();
            SubArgsAddon.updateList();
        }
    };

    public static ServerPlayersChangeEvent SERVER_PLAYERS_CHANGE_EVENT = new ServerPlayersChangeEvent() {
        @Override
        public void handle(ServerPlayersChangeEvent event) {
            SubArgsAddon.updatePlayerNames();
        }
    };

    public static ReceiveSyncEvent RECEIVE_SYNC_EVENT = new ReceiveSyncEvent() {
        @Override
        public void handle(ReceiveSyncEvent event) {
            SubArgsAddon.updateList();
        }
    };

    public static UpdatePlayerCommandsEvent UPDATE_PLAYER_COMMANDS_EVENT = new UpdatePlayerCommandsEvent() {
        @Override
        public void handle(UpdatePlayerCommandsEvent event) {
            final UUID uuid = event.getSenderObj() instanceof UUID ? (UUID) event.getSenderObj() : new CommandSender(event.getSenderObj()).getUniqueId();
            final Argument argument = SubArgsAddon.PLAYER_COMMANDS.getOrDefault(uuid, new Argument());

            argument.clearAllArguments();

            if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                for (String command : event.getCommands())
                    argument.buildArgumentStacks(command);

                for (String command : SubArgsAddon.getGroupCommands(uuid))
                    argument.buildArgumentStacks(command);
            } else {
                List<String> groupCommands = SubArgsAddon.getGroupCommands(uuid);
                for (String command : Argument.getGeneralArgument().getInputs()) {
                    if(groupCommands.contains(command)) continue;
                    argument.buildArgumentStacks(command);
                }
            }

            SubArgsAddon.PLAYER_COMMANDS.putIfAbsent(uuid, argument);
        }
    };
}
