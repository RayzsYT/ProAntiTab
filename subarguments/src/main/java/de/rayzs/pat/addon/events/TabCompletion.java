package de.rayzs.pat.addon.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
        if (event.getCompletion().isEmpty()) return;


        event.setCompletion(checkAndFilterCompletions(cursor, event.getCompletion(), SubArgsAddon.GENERAL_LIST, false));
    }

    private List<String> checkAndFilterCompletions(String cursor, List<String> possibilities, List<String> commands, boolean isGroup) {
        List<String> oldPossibilities = new ArrayList<>(possibilities);
        if (isGroup || Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            possibilities = new ArrayList<>();

        boolean nothing = false,
                emptyStart = true;

        String cursorStart = cursor.contains(" ") ? cursor.split(" ")[0] : cursor;

        for (String c : commands) {
            if(!c.startsWith(cursorStart)) continue;

            emptyStart = false;

            if (!c.startsWith(cursor)) continue;

            if(c.endsWith("_-")) {
                nothing = true;
                break;
            }

            String[] parts = c.split(" ");
            String tmpCursor = (!cursor.endsWith(" ") && parts.length > 0) ? parts[parts.length - 1] : c.substring(cursor.length());
            if (isGroup || Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                possibilities.add(tmpCursor);
                continue;
            }

            possibilities.remove(tmpCursor);
        }

        if(nothing) possibilities = new ArrayList<>();
        return !emptyStart ? possibilities : oldPossibilities;
    }
}
