package de.rayzs.pat.plugin.skript.impl.events;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.Skript;
import org.bukkit.event.Event;
import de.rayzs.pat.api.event.events.bukkit.ExecuteCommandEvent;

@Name("PAT Command Execution")
@Description("Fires after a command has been checked by PAT.")
public class ExecuteCommandSkriptEvent extends SkriptEvent {

    private boolean triggerOnBlocked;

    static {
        Skript.registerEvent("PAT Command Execution",
                ExecuteCommandSkriptEvent.class, ExecuteCommandEvent.class,
                "[if] (blocked|cancelled|not blocked|not cancelled)");
    }

    @Override
    public boolean init(Literal<?>[] literals, int matchedPattern, SkriptParser.ParseResult parseResult) {

        //  0 = blocked/cancelled
        //  1 = not blocked/cancelled
        triggerOnBlocked = matchedPattern == 0;

        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "PAT command execution event " + (triggerOnBlocked ? "(if blocked)" : "(if not blocked)");
    }

    @Override
    public boolean check(Event event) {
        if (event instanceof ExecuteCommandEvent) {
            ExecuteCommandEvent executeCommandEvent = (ExecuteCommandEvent) event;
            return executeCommandEvent.isBlocked() == triggerOnBlocked;
        }
        return false;
    }
}
