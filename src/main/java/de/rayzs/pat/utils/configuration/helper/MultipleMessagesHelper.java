package de.rayzs.pat.utils.configuration.helper;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import java.io.Serializable;
import java.util.*;

public class MultipleMessagesHelper implements Serializable {

    private final List<String> lines;

    public MultipleMessagesHelper(ConfigStorage config, String path, List<String> input) {
        final ConfigSectionHelper<ArrayList<String>> sectionHelper = new ConfigSectionHelper<>(config, path, input);
        final Object resultObj = sectionHelper.getOrSet();

        if (resultObj instanceof List list) {
            lines = (List<String>) list;
            return;
        }

        lines = input;
    }

    public List<String> getLines() {
        return lines;
    }
}
