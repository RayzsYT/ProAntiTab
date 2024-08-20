package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.api.event.PATEvent;

public abstract class ExecuteCommandEvent extends PATEvent {

    private final String command;
    private boolean blocked;

    public ExecuteCommandEvent(CommandSender sender, String command, boolean blocked) {
        super(sender);
        this.blocked = blocked;
        this.command = command;
    }

    public abstract void handle(ExecuteCommandEvent event);

    public String getCommand() {
        return command;
    }

    @Override
    public CommandSender getSender() {
        return super.getSender();
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
