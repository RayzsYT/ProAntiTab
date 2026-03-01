package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

public abstract class ServerPlayersChangeEvent extends PATEvent<ServerPlayersChangeEvent> {

    private final Type type;

    public ServerPlayersChangeEvent(CommandSender player, Type type) {
        super(player);

        this.type = type;
    }

    public ServerPlayersChangeEvent() {
        super(null);
        this.type = Type.UNKNOWN;
    }

    public enum Type { JOINED, LEFT, UNKNOWN }
}
