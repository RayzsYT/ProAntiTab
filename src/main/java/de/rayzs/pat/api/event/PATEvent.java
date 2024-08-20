package de.rayzs.pat.api.event;

import java.util.UUID;

public abstract class PATEvent<T> {

    private final UUID senderUniqueId;
    private boolean cancelled = false;

    public PATEvent(UUID senderUniqueId) {
        this.senderUniqueId = senderUniqueId;
    }

    public abstract void handle(T t);

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public UUID getSenderUniqueId() {
        return senderUniqueId;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
