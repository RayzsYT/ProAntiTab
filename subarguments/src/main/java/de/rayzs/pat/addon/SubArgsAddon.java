package de.rayzs.pat.addon;

import de.rayzs.pat.api.event.PATEventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.event.events.*;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class SubArgsAddon extends JavaPlugin {

    @Override
    public void onEnable() {

        List<String> list = Storage.Blacklist.getBlacklist().getCommands().stream().filter(command -> command.contains(" ")).toList();

        PATEventHandler.register(new FilteredTabCompletionEvent() {
            @Override
            public void handle(FilteredTabCompletionEvent event) {
                String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
                if(event.getCompletion().isEmpty()) return;

                String tmpCursor;
                List<String> possibilities = new ArrayList<>();
                for (String c : list) {
                    if(!c.startsWith(cursor)) continue;
                    tmpCursor = c.substring(cursor.length());

                    if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) possibilities.add(tmpCursor);
                    else possibilities.remove(tmpCursor);
                }

                event.setCompletion(possibilities);
            }
        });

    }
}