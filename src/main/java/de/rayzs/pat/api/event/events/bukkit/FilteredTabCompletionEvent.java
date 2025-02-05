package de.rayzs.pat.api.event.events.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List;

public abstract class FilteredTabCompletionEvent extends Event {

    private String cursor;
    private List<String> completion;

    public FilteredTabCompletionEvent() {
        this.cursor = null;
        this.completion = null;
    }

    public FilteredTabCompletionEvent(Player player, String cursor, List<String> completion) {
        this.cursor = cursor;
        this.completion = completion;
    }

    public abstract void handle(FilteredTabCompletionEvent event);

    public String getCursor() {
        return cursor;
    }

    public List<String> getCompletion() {
        return completion;
    }

    public void setCompletion(List<String> completion) {
        this.completion = completion;
    }
}
