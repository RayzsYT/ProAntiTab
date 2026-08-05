package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.List;

public abstract class UpdatePlayerCommandsEvent extends PATEvent<UpdatePlayerCommandsEvent> {

    private List<String> commands;
    private boolean serverBased;

    public UpdatePlayerCommandsEvent() {
        super(null);;
    }

    public UpdatePlayerCommandsEvent(CommandSender player, List<String> commands, boolean serverBased) {
        super(player);

        this.commands = commands;
        this.serverBased = serverBased;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isServerBased() {
        return serverBased;
    }
}
