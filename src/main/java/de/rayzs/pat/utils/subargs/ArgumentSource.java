package de.rayzs.pat.utils.subargs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArgumentSource {

    private final ArgumentBuilder GENERAL = new ArgumentBuilder(this);

    public final List<String> INPUTS = new ArrayList<>();
    public final HashMap<String, ArgumentStack> ARGUMENT_STACKS = new HashMap<>();

    public ArgumentBuilder getGeneralArgument() {
        /* Experimental removal
        ArgumentBuilder copiedGeneralArguments = new ArgumentBuilder(this);
        GENERAL.getInputs().forEach(copiedGeneralArguments::buildArgumentStacks);
        return copiedGeneralArguments;
         */
        return GENERAL;
    }

    public List<String> getOptions(String input) {
        return GENERAL.getResult(input);
    }

    public void clearArguments() {
        GENERAL.clearAllArguments();
    }

    public void buildArguments(String input) {
        GENERAL.buildArgumentStacks(input);
    }

    public List<String> getAllInputs() {
        return GENERAL.getInputs();
    }
}
