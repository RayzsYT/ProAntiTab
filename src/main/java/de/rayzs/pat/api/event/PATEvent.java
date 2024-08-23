package de.rayzs.pat.api.event;

public abstract class PATEvent<T> {

    private final Object senderObj;
    private boolean cancelled = false;

    public PATEvent(Object senderObj) {
        this.senderObj = senderObj;
    }

    public abstract void handle(T t);

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    public Object getSenderObj() {
        return senderObj;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
