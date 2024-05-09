package de.rayzs.pat.utils.configuration.yaml;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonConfiguration extends ConfigurationProvider {
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