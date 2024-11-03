package de.rayzs.pat.plugin.subargs.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.subargs.SubArgs;
import de.rayzs.pat.utils.subargs.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        String cursor = StringUtils.replaceFirst(event.getCursor(), "/", "");
        if (event.getCompletion().isEmpty()) return;

        UUID uuid = event.getSenderObj() instanceof UUID ? (UUID) event.getSenderObj() : new CommandSender(event.getSenderObj()).getUniqueId();
        Argument argument = SubArgs.PLAYER_COMMANDS.getOrDefault(uuid, Argument.getGeneralArgument());
        List<String> possibilities = event.getCompletion(), result = argument.getResult(cursor);

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            possibilities = result;
            if(result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(SubArgs.getPlayerNames());
            }

        } else {
            if(result.contains("%online_players%"))
                possibilities.removeIf(possibility -> SubArgs.getPlayerNames().contains(possibility));

            possibilities.removeAll(result);
        }

        cursor = StringUtils.getFirstArg(cursor.toLowerCase());
        if(possibilities.isEmpty() && !SubArgs.GENERAL_LIST.contains(cursor)) return;

        event.setCompletion(possibilities);
    }
}
