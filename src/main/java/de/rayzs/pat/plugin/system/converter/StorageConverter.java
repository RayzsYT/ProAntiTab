package de.rayzs.pat.plugin.system.converter;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.converter.converters.*;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class StorageConverter {

    private static StorageConverter instance;


    public static void initialize() {

        if (instance != null) {
            Logger.warning("StorageConverter already initialized!");
            return;
        }

        instance = new StorageConverter();
    }

    public static StorageConverter get() {
        return instance;
    }


    private StorageConverter() {
        registerConverter(new AdvancedPlHideConverter());
        registerConverter(new CommandWhitelistConverter());
        registerConverter(new PluginHiderPlus());
        registerConverter(new PlHideFree());
        registerConverter(new PlHidePro());
    }

    private final Map<String, Converter> converters = new HashMap<>();

    public Set<String> getConverters() {
        return converters.keySet();
    }

    public Converter getConverter(@NotNull String name) {
        return converters.get(name);
    }

    private void registerConverter(@NotNull Converter converter) {
        if (!converter.exists()) return;
        converters.putIfAbsent(converter.getPluginName(), converter);
    }
}
