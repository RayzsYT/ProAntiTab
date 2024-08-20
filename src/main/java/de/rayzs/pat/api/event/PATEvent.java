package de.rayzs.pat.api.event;

import de.rayzs.pat.utils.CommandSender;

public class PATEvent {

    private CommandSender sender;
    private boolean cancelled = false;

    public PATEvent(CommandSender sender) {
        this.sender = sender;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CommandSender getSender() {
        return sender;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
