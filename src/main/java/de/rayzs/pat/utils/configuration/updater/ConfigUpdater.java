package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigUpdater {

    private static final List<String> MISSING_PARTS = new ArrayList<>();

    private static boolean LOADED = false, AUTO_UPDATE = false, ANNOUNCE = false;

    private static final String COMPARABLE_FILE_NAME = "comparable-config.yml",
            HOW_TO_READ_FILE_NAME = "How-To-Read.txt";

    public static void initialize() {
        LOADED = false;
        MISSING_PARTS.clear();

        try {
            ConfigurationBuilder config =  Configurator.get("config");

            ANNOUNCE = (boolean) config.getOrSet("updater.announce-missing-parts", true);
            AUTO_UPDATE = (boolean) config.getOrSet("updater.auto-update-config", false);

            LOADED = true;
        } catch (Exception exception) {
            Logger.warning("Failed to find section 'updater.auto-config-updater' in config.yml!");
        }
    }

    public static boolean shouldAutoUpdate() {
        return AUTO_UPDATE;
    }

    public static void addMissingPart(String path) {
        if (!MISSING_PARTS.contains(path))
            MISSING_PARTS.add(path);
    }

    public static void broadcastMissingParts() {

        if (!LOADED)
            return;

        File outdatedConfig = new File("plugins/ProAntiTab/" + COMPARABLE_FILE_NAME);
        File howReadFile = new File("plugins/ProAntiTab/" + HOW_TO_READ_FILE_NAME);

        if (MISSING_PARTS.isEmpty()) {
            if (outdatedConfig.delete()) {
                Logger.info("Deleted the '" + outdatedConfig.getName() + "' file, because it's no longer needed.");
            }

            if (howReadFile.delete()) {
                Logger.info("Deleted the '" + howReadFile.getName() + "' file, because it's no longer needed.");
            }

            return;
        }

        if (!ANNOUNCE)
            return;

        Configurator.createResourcedFile(
                "files\\" + (Reflection.isProxyServer() ? "proxy" : "bukkit") + "-config.yml",
                COMPARABLE_FILE_NAME,
                false
        );

        Configurator.createResourcedFile(
                "files\\" + "how-to-read.txt",
                HOW_TO_READ_FILE_NAME,
                false
        );

        try {
            List<String> input = new ArrayList<>(
                    Arrays.asList(
                            "# WARNING: This file is auto generated and its sole purpose is to be used as comparison!",
                            "# WARNING: It will be deleted once this file is no longer needed.",
                            " ",
                            " "
                    )
            );

            input.addAll(
                    Files.readAllLines(Paths.get(outdatedConfig.getAbsolutePath()))
            );

            Files.write(outdatedConfig.toPath(), input);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        Logger.warning(" ");
        Logger.warning("Hey, the config.yml is missing a few parts.");
        Logger.warning("This most likely happened because PAT added a new feature or message.");
        Logger.warning(" ");
        Logger.warning("No worries though! This isn't a bad or dangerous thing.");
        Logger.warning("You can even ignore or disable this warning entirely by disabling 'announce-missing-parts'.");
        Logger.warning(" ");
        Logger.warning("But more importantly. If you want to fill the missing parts,");
        Logger.warning("then you would have three options on how to do so:");
        Logger.warning(" ");
        Logger.warning("Option 1: Delete your current config.yml and restart the server.");
        Logger.warning("Option 2: Set the missing parts/sections yourself in the config.yml.");
        Logger.warning("Option 3: Enable 'auto-update-config'. But *please* read the warning message above that option.");
        Logger.warning(" ");
        Logger.warning("To simplify this process, a new file with the newest config.yml content has been created as comparison. (plugins/ProAntiTab/comparable-config.yml)");
        Logger.warning(" ");
        Logger.warning("Following parts are missing:");

        MISSING_PARTS.forEach(missing -> {
            Logger.warning("- " + missing);
        });

        Logger.warning(" ");
        Logger.warning("You don't know how to interpret the missing parts? No worries.");
        Logger.warning("A new file has been created which explains with examples on how to read and apply the missing parts.");
        Logger.warning("-> ./plugins/ProAntiTab/" + HOW_TO_READ_FILE_NAME);
        Logger.warning(" ");
    }
}
