package de.rayzs.pat.addon.utils;

import de.rayzs.pat.addon.SubArgsAddon;
import java.util.*;

public class Argument {

    private static final Argument GENERAL = new Argument();

    private final HashMap<String, ArgumentStack> ARGUMENT_STACKS = new HashMap<>();

    public static List<String> getOptions(String input) {
        return GENERAL.getResult(input);
    }

    public static void clearArguments() {
        GENERAL.clearAllArguments();
    }

    public static void buildArguments(String input) {
        GENERAL.buildArgumentStacks(input);
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
        ARGUMENT_STACKS.clear();
    }

    public void buildArgumentStacks(String input) {
        if(!input.contains(" ")) {
            SubArgsAddon.STARTING_LIST.add(input);
            return;
        }

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
                input = input.replaceFirst(current, "");
                input = input.startsWith(" ") ? input.replaceFirst(" ", "") : input;

                for (Map.Entry<String, ArgumentStack> entry : argumentStacks.entrySet()) {
                    if(!entry.getKey().startsWith(current)) continue;
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