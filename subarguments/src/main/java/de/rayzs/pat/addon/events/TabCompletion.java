package de.rayzs.pat.addon.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
        if (event.getCompletion().isEmpty()) return;

        List<String> possibilities = event.getCompletion(),
                result = Argument.getOptions(cursor);

        if(!Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(result.contains("_*")) possibilities.clear();
            else possibilities.removeAll(result);
        } else if(!result.contains("_*")) possibilities = result;

        cursor = StringUtils.getFirstArg(cursor.toLowerCase());
        if(possibilities.isEmpty() && !SubArgsAddon.GENERAL_LIST.contains(cursor)) return;

        event.setCompletion(possibilities);
    }
}
