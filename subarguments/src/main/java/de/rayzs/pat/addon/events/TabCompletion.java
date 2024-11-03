package de.rayzs.pat.addon.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.*;
import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
        if (event.getCompletion().isEmpty()) return;

        UUID uuid = event.getSenderObj() instanceof UUID ? (UUID) event.getSenderObj() : new CommandSender(event.getSenderObj()).getUniqueId();
        Argument argument = SubArgsAddon.PLAYER_COMMANDS.getOrDefault(uuid, Argument.getGeneralArgument());
        List<String> possibilities = event.getCompletion(), result = argument.getResult(cursor);

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            possibilities = result;
            if(result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(SubArgsAddon.getPlayerNames());
            }

        } else {
            if(result.contains("%online_players%"))
                possibilities.removeIf(possibility -> SubArgsAddon.getPlayerNames().contains(possibility));

            possibilities.removeAll(result);
        }

        cursor = StringUtils.getFirstArg(cursor.toLowerCase());
        if(possibilities.isEmpty() && !SubArgsAddon.GENERAL_LIST.contains(cursor)) return;

        event.setCompletion(possibilities);
    }
}
