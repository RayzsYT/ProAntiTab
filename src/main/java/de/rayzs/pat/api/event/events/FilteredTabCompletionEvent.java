package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;

import java.util.List;

public abstract class FilteredTabCompletionEvent extends PATEvent<FilteredTabCompletionEvent> {

    private String cursor;
    private List<String> completion;

    public FilteredTabCompletionEvent() {
        super(null);
        this.cursor = null;
        this.completion = null;
    }

    public FilteredTabCompletionEvent(Object senderObj, String cursor, List<String> completion) {
        super(senderObj);
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
