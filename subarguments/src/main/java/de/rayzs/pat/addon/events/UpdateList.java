package de.rayzs.pat.addon.events;

import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.CommandSender;

import java.util.UUID;

public class UpdateList {

    public static UpdatePluginEvent UPDATE_PLUGIN_EVENT = new UpdatePluginEvent(null) {
        @Override
        public void handle(UpdatePluginEvent event) {
            SubArgsAddon.updateMessages();
            SubArgsAddon.updateList();
        }
    };

    public static ReceiveSyncEvent RECEIVE_SYNC_EVENT = new ReceiveSyncEvent(null, null) {
        @Override
        public void handle(ReceiveSyncEvent event) {
            SubArgsAddon.updateList();
        }
    };

    public static UpdatePlayerCommandsEvent UPDATE_PLAYER_COMMANDS_EVENT = new UpdatePlayerCommandsEvent(null, null, false) {
        @Override
        public void handle(UpdatePlayerCommandsEvent event) {
            CommandSender sender = new CommandSender(event.getSenderObj());
            UUID uuid = sender.getUniqueId();

        }
    };
}
