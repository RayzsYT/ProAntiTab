package de.rayzs.pat.utils.subargs;

import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentStack {

    private final HashMap<String, ArgumentStack> argumentStacks = new HashMap<>();
    private final List<String> suggestions = new ArrayList<>();

    public List<String> getResult(String input) {
        String current;
        while (input.contains(" ")) {
            current = input.split(" ")[0];

            if (suggestions.contains("%online_players%"))
                current = StringUtils.replaceElementsFromString(current, SubArguments.getPlayerNames(), "%online_players%");
            if (suggestions.contains("%hidden_online_players%"))
                current = StringUtils.replaceElementsFromString(current, SubArguments.getPlayerNames(), "%hidden_online_players%");

            input = StringUtils.replaceFirst(input, current, "");
            input = input.startsWith(" ") ? StringUtils.replaceFirst(input, " ", "") : input;

            for (Map.Entry<String, ArgumentStack> entry : argumentStacks.entrySet()) {
                if (!entry.getKey().startsWith(current)) continue;

                if (entry.getKey().contains("%online_players%"))
                    input = StringUtils.replaceElementsFromString(input, SubArguments.getPlayerNames(), "%online_players%");
                if (entry.getKey().contains("%hidden_online_players%"))
                    input = StringUtils.replaceElementsFromString(input, SubArguments.getPlayerNames(), "%hidden_online_players%");

                return entry.getValue().getResult(input);
            }
        }

        return suggestions;
    }

    public ArgumentStack createAndGetArgumentStack(String origin) {

        if (!suggestions.contains(origin))
            suggestions.add(origin);

        if (!argumentStacks.containsKey(origin)) {
            ArgumentStack argumentStack = new ArgumentStack();
            argumentStacks.put(origin, argumentStack);
            return argumentStack;
        }

        return argumentStacks.get(origin);
    }
}