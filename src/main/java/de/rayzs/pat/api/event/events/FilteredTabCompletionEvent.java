package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.*;

public abstract class FilteredTabCompletionEvent extends PATEvent<FilteredTabCompletionEvent> {

    private String cursor;
    private List<String> completion;

    public FilteredTabCompletionEvent(UUID senderUniqueId, String cursor, List<String> completion) {
        super(senderUniqueId);
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

    @Override
    public UUID getSenderUniqueId() {
        return super.getSenderUniqueId();
    }

    public void setCompletion(List<String> completion) {
        this.completion = completion;
    }
}
