package de.rayzs.pat.addon.utils;

import java.util.*;

public class Argument {

    private static final HashMap<String, ArgumentStack> ARGUMENT_STACKS = new HashMap<>();

    public static List<String> getResult(String input) {
        String firstInputArg = input.contains(" ") ? input.split(" ")[0] : input;
        return ARGUMENT_STACKS.get(input).getResult(firstInputArg);
    }

    public static void clearAllArguments() {
        ARGUMENT_STACKS.clear();
    }

    public static void buildArgumentStacks(String input) {
        if(!input.contains(" ")) {
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
                argumentStack = new ArgumentStack(s);
                ARGUMENT_STACKS.put(s, argumentStack);
            }

            first = false;
        }
    }

    public static class ArgumentStack {

        private final ArgumentStack instance;

        private final HashMap<String, ArgumentStack> argumentStacks = new HashMap<>();
        private final List<String> suggestions = new ArrayList<>();
        private final String origin;

        public ArgumentStack(String origin) {
            this.instance = this;
            this.origin = origin;
        }

        public boolean hasResult(String input) {
            return argumentStacks.containsKey(input);
        }

        public List<String> getResult(String input) {
            ArgumentStack tmpArgumentStack = this;

            while (!input.contains(" ")) {
                if (!tmpArgumentStack.hasResult(input)) break;

                input = input.contains(" ") ? input.substring(0, origin.length() + 1) : input;
                tmpArgumentStack = getInstance();
            }

            return tmpArgumentStack.getResult(input);
        }

        public String getOrigin() {
            return origin;
        }

        public ArgumentStack getInstance() {
            return instance;
        }

        public ArgumentStack createAndGetArgumentStack(String origin) {
            if(!argumentStacks.containsKey(origin)) {
                ArgumentStack argumentStack = new ArgumentStack(origin);
                argumentStacks.put(origin, argumentStack);
                return argumentStack;
            }

            return argumentStacks.get(origin);
        }
    }
}