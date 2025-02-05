package de.rayzs.pat.plugin.skript.impl.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import de.rayzs.pat.api.event.events.bukkit.ExecuteCommandEvent;
import org.bukkit.event.Event;

@Name("PAT Command Execution")
@Description("Fires after a command has been checked by PAT.")
public class ExecuteCommandSkriptEvent extends SkriptEvent {

    static {
        Skript.registerEvent("PAT Command Execution",
                ExecuteCommandSkriptEvent.class, ExecuteCommandEvent.class,
                "[if] (blocked|cancelled|not blocked|not cancelled)");
    }

    @Override
    public boolean init(Literal<?>[] literals, int i, SkriptParser.ParseResult parseResult) {

        return false;
    }

    @Override
    public String toString(Event event, boolean b) {

        return null;
    }

    @Override
    public boolean check(Event event) {

        if (event instanceof ExecuteCommandEvent) {
            ExecuteCommandEvent executeCommandEvent = (ExecuteCommandEvent) event;
            return executeCommandEvent.isBlocked();
        }

        return false;
    }
}
