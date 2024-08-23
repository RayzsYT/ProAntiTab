package de.rayzs.pat.addon;

import de.rayzs.pat.api.event.PATEventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class SubArgsAddon extends JavaPlugin {

    private static List<String> GENERAL_LIST;

    @Override
    public void onEnable() {
        updateGeneralList();

        PATEventHandler.register(new UpdatePluginEvent(null) {
            public void handle(UpdatePluginEvent updatePluginEvent) {
                SubArgsAddon.this.updateGeneralList();
            }
        });

        PATEventHandler.register(new ReceiveSyncEvent(null, null) {
            public void handle(ReceiveSyncEvent receiveSyncEvent) {
                SubArgsAddon.this.updateGeneralList();
            }
        });

        PATEventHandler.register(new ExecuteCommandEvent() {
            public void handle(ExecuteCommandEvent event) {
                String command = StringUtils.replaceFirst(event.getCommand(), "/", "");
                boolean listed = false, spaces = false, equals = false,
                        turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED,
                        blocked = event.isBlocked();

                for (String s : GENERAL_LIST) {
                    if (!s.toLowerCase().startsWith(command.toLowerCase())) continue;
                    if (!listed) listed = true;

                    if (!equals && s.equalsIgnoreCase(command))
                        equals = true;

                    else if (!spaces && s.contains(" "))
                        spaces = true;
                }



            }
        });

        PATEventHandler.register(new FilteredTabCompletionEvent() {
            public void handle(FilteredTabCompletionEvent event) {
                String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
                if (event.getCompletion().isEmpty()) return;


                event.setCompletion(checkAndFilterCompletions(cursor, event.getCompletion(), SubArgsAddon.GENERAL_LIST, false));
            }
        });
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

    private void updateGeneralList() {
        GENERAL_LIST = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).toList();
    }
}