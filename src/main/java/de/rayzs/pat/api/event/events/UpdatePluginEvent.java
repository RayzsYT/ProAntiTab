package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

public abstract class UpdatePluginEvent extends PATEvent<UpdatePluginEvent> {

    public UpdatePluginEvent() {
        super(null);;
    }

    public UpdatePluginEvent(CommandSender player) {
        super(player);
    }
}
