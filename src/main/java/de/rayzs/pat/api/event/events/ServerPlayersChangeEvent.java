package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;

public abstract class ServerPlayersChangeEvent extends PATEvent<ServerPlayersChangeEvent> {

    private final Type type;

    public ServerPlayersChangeEvent(Object senderObj, Type type) {
        super(senderObj);
        this.type = type;
    }

    public ServerPlayersChangeEvent() {
        super(null);
        this.type = Type.UNKNOWN;
    }

    public enum Type { JOINED, LEFT, UNKNOWN }
}
