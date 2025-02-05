package de.rayzs.pat.api.event.events.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List;

public abstract class FilteredSuggestionEvent extends Event {

    private List<String> suggestions;

    public FilteredSuggestionEvent() {
        this.suggestions = null;
    }

    public FilteredSuggestionEvent(Player player, List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
