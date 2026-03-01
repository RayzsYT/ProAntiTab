package de.rayzs.pat.plugin.system.subargument.argument;

import de.rayzs.pat.api.storage.Storage;

import java.util.List;

public class Arguments {

    private static final Arguments instance = new Arguments();

    public static Arguments get() {
        return instance;
    }


    private Arguments() {}

    public final ArgumentSource CHAT_ARGUMENTS = new ArgumentSource();
    public final ArgumentSource TAB_ARGUMENTS = new ArgumentSource();

    public List<String> getResultChat(String input) {
        return CHAT_ARGUMENTS.getGeneralArgument().getResult(input);
    }

    public List<String> getResultTab(String input) {
        return TAB_ARGUMENTS.getGeneralArgument().getResult(input);
    }

    public void buildArgumentStacks(String input) {
        Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(input);

        if (type != Storage.Blacklist.BlockType.NEGATE) {
            input = Storage.Blacklist.BlockTypeFetcher.modify(input, type);
        }

        switch (type) {
            case TAB:
                TAB_ARGUMENTS.getGeneralArgument().buildArgumentStacks(input);
                break;

            case CHAT:
                CHAT_ARGUMENTS.getGeneralArgument().buildArgumentStacks(input);
                break;

            case BOTH: default:
                CHAT_ARGUMENTS.buildArguments(input);
                TAB_ARGUMENTS.buildArguments(input);
                break;
        }
    }

    public void clearArguments() {
        CHAT_ARGUMENTS.clearArguments();
        TAB_ARGUMENTS.clearArguments();
    }
}
