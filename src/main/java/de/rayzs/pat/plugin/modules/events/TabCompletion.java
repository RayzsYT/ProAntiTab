package de.rayzs.pat.plugin.modules.events;

import de.rayzs.pat.api.event.events.FilteredTabCompletionEvent;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import de.rayzs.pat.utils.subargs.Arguments;

import java.util.*;

public class TabCompletion extends FilteredTabCompletionEvent {

    @Override
    public void handle(FilteredTabCompletionEvent event) {
        final String cursor = event.getCursor().substring(1);

        if (event.getCompletion().isEmpty()) return;

        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        final UUID uuid = event.getSenderObj() instanceof UUID
                ? (UUID) event.getSenderObj()
                : CommandSenderHandler.from(event.getSenderObj()).getUniqueId();

        final Arguments arguments = SubArgsModule.PLAYER_COMMANDS.getOrDefault(uuid, Arguments.ARGUMENTS);

        List<String> possibilities = event.getCompletion(), result = new ArrayList<>(arguments.getResultTab(cursor));
        final List<String> negated = new ArrayList<>(arguments.getResultTab("!" + cursor));

        final List<String> negatedInputs = new ArrayList<>(arguments.TAB_ARGUMENTS.INPUTS).stream().filter(s -> {
            if (s.isEmpty())
                return false;

            boolean n = s.charAt(0) == '!';

            if (!n && s.length() > 5)
                n = s.charAt(5) == '!';

            return n;
        }).map(s -> s.substring(1 + (s.charAt(0) == '[' ? 5 : 0))).toList();

        if (negatedInputs.stream().anyMatch(cursor::startsWith)) {
            if (turn) {
                event.setCompletion(new ArrayList<>());
            }

            return;
        }

        result.removeIf(s -> s.contains("-_"));

        if (turn) {
            possibilities = result;

            if (possibilities.isEmpty() && !negated.isEmpty()) {
                possibilities = event.getCompletion();
                possibilities.removeAll(negated);
            }

            if (result.contains("%numbers%")) {
                possibilities.remove("%numbers%");
                possibilities.addAll(event.getCompletion().stream().filter(NumberUtils::isDigit).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");
            }

            if (result.contains("%players%")) {
                possibilities.remove("%players%");
                possibilities.addAll(SubArgsModule.getPlayerNames());
            }

            if (result.contains("%online_players%")) {
                possibilities.remove("%online_players%");
                possibilities.addAll(SubArgsModule.getOnlinePlayerNames());
            }

            if (result.contains("%hidden_players%")) {
                possibilities.remove("%hidden_players%");

                possibilities.addAll(event.getCompletion().stream().filter(completion -> {
                    if (SubArgsModule.getPlayerNames().contains(completion))
                        return false;

                    return result.contains(completion);
                }).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");

            }

            if (result.contains("%hidden_online_players%")) {
                possibilities.remove("%hidden_online_players%");

                possibilities.addAll(event.getCompletion().stream().filter(completion -> {
                    if (SubArgsModule.getOnlinePlayerNames().contains(completion))
                        return false;

                    return result.contains(completion);
                }).toList());

                if (possibilities.isEmpty())
                    possibilities.add("///////////");
            }

        } else {
            if (result.contains("%numbers%"))
                possibilities.removeIf(NumberUtils::isDigit);

            if (result.contains("%players%"))
                possibilities.removeIf(possibility -> SubArgsModule.getPlayerNames().contains(possibility));

            if (result.contains("%online_players%"))
                possibilities.removeIf(possibility -> SubArgsModule.getOnlinePlayerNames().contains(possibility));


            possibilities.removeIf(s -> {
                if (negated.contains(s)) {
                    return false;
                }

                return result.contains(s);
            });
        }

        String firstCursorArg = StringUtils.getFirstArg(cursor.toLowerCase());
        if (possibilities.isEmpty() && !SubArgsModule.GENERAL_LIST.contains(firstCursorArg)) {
            return;
        }

        event.setCompletion(possibilities);
    }
}
