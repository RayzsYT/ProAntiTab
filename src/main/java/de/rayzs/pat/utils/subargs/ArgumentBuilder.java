package de.rayzs.pat.utils.subargs;

import java.util.*;

public class ArgumentBuilder {

    private final ArgumentSource source;

    public ArgumentBuilder(ArgumentSource source) {
        this.source = source;
    }

    public List<String> getInputs() {
        return source.INPUTS;
    }

    public List<String> getResult(String input) {
        String firstInputArg = input.contains(" ") ? input.split(" ")[0] : input;

        for (Map.Entry<String, ArgumentStack> entry : source.ARGUMENT_STACKS.entrySet()) {
            if(!entry.getKey().equals(firstInputArg)) continue;
            return source.ARGUMENT_STACKS.get(entry.getKey()).getResult(input);
        }

        return Collections.emptyList();
    }

    public void clearAllArguments() {
        source.INPUTS.clear();
        source.ARGUMENT_STACKS.clear();
    }

    public void buildArgumentStacks(String input) {
        if(!input.contains(" ")) return;

        if(!source.INPUTS.contains(input))
            source.INPUTS.add(input);

        boolean first = true;
        ArgumentStack argumentStack = null;

        for (String s : input.split(" ")) {
            if(!first) {
                argumentStack = argumentStack.createAndGetArgumentStack(s);
                continue;
            }

            argumentStack = source.ARGUMENT_STACKS.get(s);
            if (argumentStack == null) {
                argumentStack = new ArgumentStack();
                source.ARGUMENT_STACKS.put(s, argumentStack);
            }

            first = false;
        }
    }
}
