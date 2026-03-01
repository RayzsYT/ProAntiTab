package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

public abstract class FilteredTabCompletionEvent extends PATEvent<FilteredTabCompletionEvent> {

    private String cursor;
    private List<String> completion;

    public FilteredTabCompletionEvent() {
        super(null);

        this.cursor = null;
        this.completion = null;
    }

    public FilteredTabCompletionEvent(CommandSender player, String cursor, List<String> completion) {
        super(player);

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
