package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

public abstract class FilteredSuggestionEvent extends PATEvent<FilteredSuggestionEvent> {

    private HashSet<String> suggestions;

    public FilteredSuggestionEvent() {
        super(null);
        this.suggestions = null;
    }

    public FilteredSuggestionEvent(CommandSender player, HashSet<String> suggestions) {
        super(player);

        this.suggestions = suggestions;
    }

    public HashSet<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(HashSet<String> suggestions) {
        this.suggestions = suggestions;
    }
}
