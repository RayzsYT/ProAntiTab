package de.rayzs.pat.api.event.events.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class ExecuteCommandEvent extends Event implements Cancellable {

    private final String command;
    private boolean blocked;

    public ExecuteCommandEvent() {
        this.blocked = false;
        this.command = null;
    }

    public ExecuteCommandEvent(Player player, String command, boolean blocked) {
        this.blocked = blocked;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public boolean isCancelled() {
        return blocked;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.blocked = cancel;
    }
}
