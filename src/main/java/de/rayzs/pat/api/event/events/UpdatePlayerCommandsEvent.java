package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.List;

public abstract class UpdatePlayerCommandsEvent extends PATEvent<UpdatePlayerCommandsEvent> {

    public UpdatePlayerCommandsEvent() {
        super(null);;
    }

    public UpdatePlayerCommandsEvent(Object senderObj, List<String> commands, boolean serverBased) {
        super(senderObj);
    }
}
