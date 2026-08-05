package de.rayzs.pat.api.event;

import de.rayzs.pat.utils.sender.CommandSender;

public abstract class PATEvent<T> {

    private final CommandSender player;
    private boolean cancelled = false;

    public PATEvent(CommandSender player) {
        this.player = player;
    }

    public abstract void handle(T t);

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    public CommandSender getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
