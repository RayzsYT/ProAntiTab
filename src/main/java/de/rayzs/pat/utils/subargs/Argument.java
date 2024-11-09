package de.rayzs.pat.utils.subargs;

import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class Argument {

    private static final Argument GENERAL = new Argument();

    private final List<String> INPUTS = new ArrayList<>();
    private final HashMap<String, ArgumentStack> ARGUMENT_STACKS = new HashMap<>();

    public static Argument getGeneralArgument() {
        Argument copiedGeneralArguments = new Argument();
        GENERAL.getInputs().forEach(copiedGeneralArguments::buildArgumentStacks);
        return copiedGeneralArguments;
    }

    public static List<String> getOptions(String input) {
        return GENERAL.getResult(input);
    }

    public static void clearArguments() {
        GENERAL.clearAllArguments();
    }

    public static void buildArguments(String input) {
        GENERAL.buildArgumentStacks(input);
    }

    public static List<String> getAllInputs() {
        return GENERAL.getInputs();
    }


    public List<String> getInputs() {
        return INPUTS;
    }

    public List<String> getResult(String input) {
        String firstInputArg = input.contains(" ") ? input.split(" ")[0] : input;

        for (Map.Entry<String, ArgumentStack> entry : ARGUMENT_STACKS.entrySet()) {
            if(!entry.getKey().startsWith(firstInputArg)) continue;
            return ARGUMENT_STACKS.get(entry.getKey()).getResult(input);
        }

        return Collections.emptyList();
    }

    public void clearAllArguments() {
        INPUTS.clear();
        ARGUMENT_STACKS.clear();
    }

    public void buildArgumentStacks(String input) {
        if(!input.contains(" ")) return;

        if(!INPUTS.contains(input))
            INPUTS.add(input);

        boolean first = true;
        ArgumentStack argumentStack = null;

        for (String s : input.split(" ")) {
            if(!first) {
                argumentStack = argumentStack.createAndGetArgumentStack(s);
                continue;
            }

            argumentStack = ARGUMENT_STACKS.get(s);
            if (argumentStack == null) {
                argumentStack = new ArgumentStack();
                ARGUMENT_STACKS.put(s, argumentStack);
            }

            first = false;
        }
    }

    private static class ArgumentStack {

        private final HashMap<String, ArgumentStack> argumentStacks = new HashMap<>();
        private final List<String> suggestions = new ArrayList<>();

        public List<String> getResult(String input) {
            String current;
            while(input.contains(" ")) {
                current = input.split(" ")[0];

                if(suggestions.contains("%online_players%"))
                    current = StringUtils.replaceElementsFromString(current, SubArgsModule.getPlayerNames(), "%online_players%");
                if(suggestions.contains("%hidden_online_players%"))
                    current = StringUtils.replaceElementsFromString(current, SubArgsModule.getPlayerNames(), "%hidden_online_players%");

                input = StringUtils.replaceFirst(input, current, "");
                input = input.startsWith(" ") ? StringUtils.replaceFirst(input, " ", "") : input;

                for (Map.Entry<String, ArgumentStack> entry : argumentStacks.entrySet()) {
                    if(!entry.getKey().startsWith(current)) continue;

                    if(entry.getKey().contains("%online_players%"))
                        input = StringUtils.replaceElementsFromString(input, SubArgsModule.getPlayerNames(), "%online_players%");
                    if(entry.getKey().contains("%hidden_online_players%"))
                        input = StringUtils.replaceElementsFromString(input, SubArgsModule.getPlayerNames(), "%hidden_online_players%");

                    return entry.getValue().getResult(input);
                }
            }

            return suggestions;
        }

        public ArgumentStack createAndGetArgumentStack(String origin) {

            if(!suggestions.contains(origin))
                suggestions.add(origin);

            if(!argumentStacks.containsKey(origin)) {
                ArgumentStack argumentStack = new ArgumentStack();
                argumentStacks.put(origin, argumentStack);
                return argumentStack;
            }

            return argumentStacks.get(origin);
        }
    }
}