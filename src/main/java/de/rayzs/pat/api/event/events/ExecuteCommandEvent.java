package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.UUID;

public abstract class ExecuteCommandEvent extends PATEvent<ExecuteCommandEvent> {

    private final String command;
    private boolean blocked;

    public ExecuteCommandEvent(UUID senderUniqueId, String command, boolean blocked) {
        super(senderUniqueId);
        this.blocked = blocked;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public UUID getSenderUniqueId() {
        return super.getSenderUniqueId();
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
