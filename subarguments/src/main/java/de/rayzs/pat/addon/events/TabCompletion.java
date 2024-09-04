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

        boolean hideAll = false, showAll = false,
                emptyStart = true, hideAfter = false;

        String cursorStart = cursor;
        int cursorSpaces = 0, spaces = 0;

        if(cursor.contains(" ")) {
            String[] cursorParts = cursor.split(" ");
            cursorSpaces = cursorParts.length;
            cursorStart = cursorParts[0];
        }

        for (String c : commands) {
            if (!c.startsWith(cursorStart)) continue;

            emptyStart = false;

            if (!c.startsWith(cursor)) continue;

            if (c.contains(" "))
                spaces = c.split(" ").length - 1;

            String[] parts = c.split(" ");
            String tmpCursor = (!cursor.endsWith(" ") && parts.length > 0) ? parts[parts.length - 1] : c.substring(cursor.length());

            if (cursorSpaces == spaces) {
                if (tmpCursor.equals("_-")) {
                    hideAll = true;
                    break;
                }

                if (tmpCursor.equals("_*")) {
                    showAll = true;
                    break;
                }

            } else if (cursorSpaces >= spaces && tmpCursor.equals("_--")) {
                hideAfter = true;
                break;


            } else if (cursorSpaces > spaces) {
                //if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            }
            if (isGroup || Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
                possibilities.add(tmpCursor);
                continue;
            }

            possibilities.remove(tmpCursor);
        }

        if(hideAll || hideAfter) possibilities = new ArrayList<>();
        else if(showAll) possibilities = new ArrayList<>(oldPossibilities);

        return !emptyStart ? possibilities : oldPossibilities;
    }
}
