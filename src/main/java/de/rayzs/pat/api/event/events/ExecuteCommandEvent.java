package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;

public abstract class ExecuteCommandEvent extends PATEvent<ExecuteCommandEvent> {

    private final String command;
    private boolean blocked, notify;

    public ExecuteCommandEvent() {
        super(null);
        this.blocked = false;
        this.notify = false;
        this.command = null;
    }

    public ExecuteCommandEvent(Object senderObj, String command, boolean blocked, boolean notify) {
        super(senderObj);
        this.blocked = blocked;
        this.command = command;
        this.notify = notify;
    }

    public void setDoesNotify(boolean notify) {
        this.notify = notify;
    }

    public String getCommand() {
        return command;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean doesNotify() {
        return notify;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
