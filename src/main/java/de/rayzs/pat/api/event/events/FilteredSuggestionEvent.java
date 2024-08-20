package de.rayzs.pat.api.event.events;

import de.rayzs.pat.api.event.PATEvent;
import de.rayzs.pat.utils.CommandSender;

import java.util.List;

public abstract class FilteredSuggestionEvent extends PATEvent {

    private String cursor;
    private List<String> suggestions;

    public FilteredSuggestionEvent(CommandSender sender, String cursor, List<String> suggestions) {
        super(sender);
        this.cursor = cursor;
        this.suggestions = suggestions;
    }

    public abstract void handle(FilteredSuggestionEvent event);

    public String getCursor() {
        return cursor;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    public CommandSender getSender() {
        return super.getSender();
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
