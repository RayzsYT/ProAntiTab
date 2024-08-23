package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.UUID;

public abstract class ExecuteCommandEvent extends PATEvent<ExecuteCommandEvent> {

    private final String command;
    private boolean blocked;

    public ExecuteCommandEvent() {
        super(null);
        this.blocked = false;
        this.command = null;
    }

    public ExecuteCommandEvent(Object senderObj, String command, boolean blocked) {
        super(senderObj);
        this.blocked = blocked;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
