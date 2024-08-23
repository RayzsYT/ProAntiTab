package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.UUID;

public abstract class UpdatePluginEvent extends PATEvent<UpdatePluginEvent> {

    public UpdatePluginEvent() {
        super(null);;
    }

    public UpdatePluginEvent(UUID senderUniqueId) {
        super(senderUniqueId);
    }
}
