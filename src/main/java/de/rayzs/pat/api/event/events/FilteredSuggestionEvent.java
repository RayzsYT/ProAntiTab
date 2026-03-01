package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

public abstract class FilteredSuggestionEvent extends PATEvent<FilteredSuggestionEvent> {

    private List<String> suggestions;

    public FilteredSuggestionEvent() {
        super(null);
        this.suggestions = null;
    }

    public FilteredSuggestionEvent(CommandSender player, List<String> suggestions) {
        super(player);

        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
