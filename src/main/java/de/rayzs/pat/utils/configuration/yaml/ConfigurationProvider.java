package de.rayzs.pat.utils.configuration.yaml;

import java.util.*;
import java.io.*;

public abstract class ConfigurationProvider {

    /*
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/config/src/main/java/net/md_5/bungee/config/ConfigurationProvider.java
     */

    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> providers = new HashMap<>();

    static {
        try {
            providers.put(YamlConfiguration.class, new YamlConfiguration());
        } catch (NoClassDefFoundError noClassDefFoundError) {}
        try {
            providers.put(JsonConfiguration.class, new JsonConfiguration());
        } catch (NoClassDefFoundError noClassDefFoundError) {}
    }

    public abstract Configuration load(String paramString, Configuration paramConfiguration);

    public abstract Configuration load(String paramString);

    public abstract Configuration load(InputStream paramInputStream, Configuration paramConfiguration);

    public abstract Configuration load(InputStream paramInputStream);

    public abstract Configuration load(Reader paramReader, Configuration paramConfiguration);

    public abstract Configuration load(Reader paramReader);

    public abstract Configuration load(File paramFile, Configuration paramConfiguration) throws IOException;

    public abstract Configuration load(File paramFile) throws IOException;

    public abstract void save(Configuration paramConfiguration, Writer paramWriter);

    public abstract void save(Configuration paramConfiguration, File paramFile) throws IOException;

    public static ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider) {
        return providers.get(provider);
    }
}
