package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;

public abstract class UpdatePluginEvent extends PATEvent<UpdatePluginEvent> {

    public UpdatePluginEvent() {
        super(null);
        ;
    }

    public UpdatePluginEvent(Object senderObj) {
        super(senderObj);
    }
}
