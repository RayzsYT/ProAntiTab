package de.rayzs.pat.utils.configuration.yaml;

import com.google.common.base.Charsets;
import java.lang.reflect.Type;
import com.google.gson.*;
import java.util.*;
import java.io.*;

public class JsonConfiguration extends ConfigurationProvider {

    /*
        Original code from Bungeecord source:
        https://github.com/SpigotMC/BungeeCord/blob/master/config/src/main/java/net/md_5/bungee/config/JsonConfiguration.java
     */

    private final Gson json = (new GsonBuilder()).serializeNulls().setPrettyPrinting().registerTypeAdapter(Configuration.class, new JsonSerializer<Configuration>() {
        public JsonElement serialize(Configuration src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.self);
        }
    }).create();

    public void save(Configuration config, File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            save(config, writer);
        }
    }

    public void save(Configuration config, Writer writer) {
        this.json.toJson(config.self, writer);
    }

    public Configuration load(File file) throws IOException {
        return load(file, (Configuration)null);
    }

    public Configuration load(File file, Configuration defaults) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return load(is, defaults);
        }
    }

    public Configuration load(Reader reader) {
        return load(reader, (Configuration)null);
    }

    public Configuration load(Reader reader, Configuration defaults) {
        Map<String, Object> map = (Map<String, Object>)this.json.fromJson(reader, LinkedHashMap.class);
        if (map == null)
            map = new LinkedHashMap<>();
        return new Configuration(map, defaults);
    }

    public Configuration load(InputStream is) {
        return load(is, (Configuration)null);
    }

    public Configuration load(InputStream is, Configuration defaults) {
        return load(new InputStreamReader(is, Charsets.UTF_8), defaults);
    }

    public Configuration load(String string) {
        return load(string, (Configuration)null);
    }

    public Configuration load(String string, Configuration defaults) {
        Map<String, Object> map = (Map<String, Object>)this.json.fromJson(string, LinkedHashMap.class);
        if (map == null)
            map = new LinkedHashMap<>();
        return new Configuration(map, defaults);
    }
}