package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;

import java.util.List;

public abstract class FilteredSuggestionEvent extends PATEvent<FilteredSuggestionEvent> {

    private List<String> suggestions;

    public FilteredSuggestionEvent() {
        super(null);
        this.suggestions = null;
    }

    public FilteredSuggestionEvent(Object senderObj, List<String> suggestions) {
        super(senderObj);
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
