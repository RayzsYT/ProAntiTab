package de.rayzs.pat.plugin.converter;

import de.rayzs.pat.plugin.converter.converters.CommandWhitelist;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class StorageConverter {

    private StorageConverter() {}

    private static final Map<String, Converter> converters = new HashMap<>();


    public static void initialize() {

        if (!converters.isEmpty()) {
            return;
        }

        registerConverter(new CommandWhitelist());
    }

    public static Set<String> getConverters() {
        return converters.keySet();
    }

    public static Converter getConverter(@NotNull String name) {
        return converters.get(name);
    }

    public static void registerConverter(@NotNull Converter converter) {
        if (!converter.exists()) return;

        converters.putIfAbsent(converter.getPluginName(), converter);
    }
}
