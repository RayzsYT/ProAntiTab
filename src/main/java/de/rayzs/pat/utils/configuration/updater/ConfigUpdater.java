package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.*;
import java.util.*;
import java.io.*;

public class ConfigUpdater {

    private static List<String> NEWEST_CONFIG_INPUT = new ArrayList<>();

    public static void initialize() {
        NEWEST_CONFIG_INPUT = new ConnectionBuilder().setUrl("https://raw.githubusercontent.com/RayzsYT/ProAntiTab/main/src/main/resources/files/"
                + (Reflection.isProxyServer() ? " proxy" : "bukkit")
                + "-config.yml").connect().getResponseList();
    }

    public static void updateConfigFile(ConfigurationBuilder configurationBuilder, int atLine, int from, int to) {
        File file = configurationBuilder.getFile();
        ConfigSection configSection = new ConfigSection(file);
        String sectionAsString = StringUtils.buildStringList(configSection.createAndGetNewFileInput(atLine, from, to));

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false));
            writer.write(sectionAsString);
            writer.close();
        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static List<String> getNewestConfigInput() {
        return NEWEST_CONFIG_INPUT;
    }
}
