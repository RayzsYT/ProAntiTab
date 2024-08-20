package de.rayzs.pat.api.event.events;

import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.api.event.PATEvent;
import java.util.List;

public abstract class FilteredTabCompletionEvent extends PATEvent {

    private String command;
    private List<String> suggestions;

    public FilteredTabCompletionEvent(CommandSender sender, String command, List<String> suggestions) {
        super(sender);
        this.command = command;
        this.suggestions = suggestions;
    }

    public abstract void handle(FilteredTabCompletionEvent event);

    public String getCursor() {
        return command;
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
