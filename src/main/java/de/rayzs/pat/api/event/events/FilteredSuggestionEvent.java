package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import java.util.*;

public abstract class FilteredSuggestionEvent extends PATEvent<FilteredSuggestionEvent> {

    private List<String> suggestions;

    public FilteredSuggestionEvent(UUID senderUniqueId, List<String> suggestions) {
        super(senderUniqueId);
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public UUID getSenderUniqueId() {
        return super.getSenderUniqueId();
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
