package de.rayzs.pat.utils.configuration.helper;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import java.util.*;

public class MultipleMessagesHelper {

    private final ArrayList<String> lines;

    public MultipleMessagesHelper(ConfigStorage config, String path, List<String> input) {
        ConfigSectionHelper<ArrayList<String>> sectionHelper = new ConfigSectionHelper<>(config, path, input);

        if(!sectionHelper.exist()) {
            lines = new ArrayList<>(input);
            sectionHelper.set(lines);
            return;
        }

        lines = sectionHelper.get();
    }

    public ArrayList<String> getLines() {
        return lines;
    }
}
