package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class ConfigUpdater {

    private static List<String> NEWEST_CONFIG_INPUT = new ArrayList<>(), MISSING_PARTS = new ArrayList<>();
    private static boolean LOADED = false;

    public static void initialize() {
        NEWEST_CONFIG_INPUT = new ConnectionBuilder().setUrl("https://raw.githubusercontent.com/RayzsYT/ProAntiTab/main/src/main/resources/files/"
                + (Reflection.isProxyServer() ? "proxy" : "bukkit")
                + "-config.yml").connect().getResponseList();

        LOADED = !NEWEST_CONFIG_INPUT.isEmpty();
    }

    public static void updateConfigFile(ConfigurationBuilder configurationBuilder, String target, boolean section) {
        configurationBuilder.reload();
        int[] position = section ? getPositionBySection(NEWEST_CONFIG_INPUT, target, true) : getSectionPositionByTarget(NEWEST_CONFIG_INPUT, target, true);
        updateConfigFile(configurationBuilder.getFile(), target, position[0], position[0], position[1]);
    }

    public static void addMissingPart(String part) {
        MISSING_PARTS.add(part);
    }

    public static void broadcastMissingParts() {
        File outdatedConfig = new File("./plugins/ProAntiTab/comparable-config.yml");
        if (MISSING_PARTS.isEmpty()) {
            if (outdatedConfig.delete())
                Logger.info("Deleted 'comparable-config.yml' because it's not needed anymore.");
            return;
        }

        List<String> fileInput = new LinkedList<>(Arrays.asList("# WARNING: This file is auto generated with to solely purpose to be used as comparison!", " ", " "));
        fileInput.addAll(NEWEST_CONFIG_INPUT);

        try {
            outdatedConfig.createNewFile();
            Files.write(outdatedConfig.toPath(), fileInput);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        Logger.warning("The config.yml is missing a few parts that can't be replaced automatically.");
        Logger.warning("What can be done to solve this issue? There are two options:");
        Logger.warning("Option 1: Delete your current config.yml and restart the server.");
        Logger.warning("Option 2: Set the missing parts/sections yourself in the config.yml.");
        Logger.warning("To simplify that process, a new file with the newest config.yml content has been created as comparison. (plugins/ProAntiTab/comparable-config.yml)");
        Logger.warning(" ");

        HashMap<String, List<String>> map = new HashMap<>();
        String section, part;
        List<String> list;

        for (String missingPart : MISSING_PARTS) {
            section = getSection(missingPart, false);
            part = getVariable(missingPart);

            list = map.getOrDefault(section, new ArrayList<>());
            if (!list.contains(part)) list.add(part);

            map.putIfAbsent(section, list);
        }

        int[] line;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Logger.warning("Missing parts at section " + entry.getKey() + ":");
            for (String missingPart : entry.getValue()) {
                line = getSectionPositionByTarget(fileInput, entry.getKey() + "." + missingPart, false);
                Logger.warning(" - " + missingPart + " [in comparable-config.yml at line " + (line[1] + 1) + "]");
            }
        }
    }

    private static void updateConfigFile(File file, String target, int atLine, int from, int to) {
        ConfigSection configSection = new ConfigSection(file);

        try {
            Files.write(file.toPath(), configSection.createAndGetNewFileInput(atLine, from, to));
        } catch (Exception exception) {
            Logger.warning("Failed to read file input! (#2)");
            exception.printStackTrace();
        }
    }

    public static String getVariable(String target) {
        if (target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            target = pathSplit[pathSplit.length - 1];
        }

        return target;
    }

    public static String getSection(String target, boolean first) {
        if (target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            if (first) return pathSplit.length > 0 ? pathSplit[0] : target;

            LinkedList<String> pathList = new LinkedList<>(Arrays.asList(pathSplit));
            pathList.remove(pathList.size() - 1);
            target = StringUtils.buildStringListWithoutColors(pathList, ".");
        }

        return target;
    }

    public static boolean canUpdate() {
        return LOADED;
    }

    public static int[] getSectionPositionByTarget(List<String> lines, String targetPath, boolean comments) {
        HashMap<Integer, String> hash = new HashMap<>();
        int sections = 0;

        String target = targetPath;
        if (target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            sections = (pathSplit.length - 1) * 2;

            target = pathSplit[pathSplit.length - 1];
        }

        String line;
        int i;
        for (i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            if (line.isEmpty() || line.startsWith("#") || !line.contains(":")) continue;
            line = line.split(":")[0];

            if (!StringUtils.remove(line, " ").equals(target)) continue;
            if (StringUtils.countLetters(line, ' ', true) != sections) continue;
            hash.put(i, line);
        }

        StringBuilder sectionPath;
        int position, spaces, removeSpaces, finalStartPos = 0, finalEndPos, oldSections = sections;

        for (Map.Entry<Integer, String> entry : hash.entrySet()) {
            line = entry.getValue();
            position = entry.getKey();
            finalEndPos = entry.getKey();
            removeSpaces = 2;
            sectionPath = new StringBuilder(StringUtils.remove(line, " "));
            sections = oldSections;
            sections -= 2;

            do {
                line = StringUtils.getLineText(lines, position);
                spaces = StringUtils.countLetters(line, ' ', true);

                if (spaces == sections) {
                    sections -= removeSpaces;
                    line = StringUtils.remove((line != null && line.contains(":") ? line.split(":")[0] : line), " ");
                    finalStartPos = position;
                    sectionPath.insert(0, line + ".");
                }

                if (position < 0) break;
                position--;
            } while (spaces != 0);

            if (sectionPath.toString().equals(targetPath)) {
                finalStartPos = targetPath.contains(".") ? finalStartPos : finalEndPos;

                if (comments) {
                    int start = finalStartPos - 1;
                    for (i = start; i > 0; i--) {
                        line = StringUtils.getLineText(lines, i);
                        if (line != null && !line.startsWith("#")) {
                            finalStartPos = i + 1;
                            break;
                        }
                    }
                }

                return new int[]{finalStartPos, finalEndPos};
            }
        }

        return new int[]{0, 0};
    }

    public static int[] getPositionBySection(List<String> lines, String target, boolean comments) {
        int start = getSectionPositionByTarget(lines, target, comments)[0], end = start, i;
        String line;

        if (target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            target = pathSplit[0];
        }

        boolean reachedSection = false;

        for (i = start; i < lines.size(); i++) {
            line = lines.get(i);

            if (line.contains(":")) line = line.split(":")[0];
            if (!reachedSection && target.equals(StringUtils.remove(line, " ")))
                reachedSection = true;

            if (line == null) continue;

            if (line.startsWith("#")) {
                if (!reachedSection) continue;
                end = i - 1;
                break;
            }

            if (StringUtils.countLetters(line, ' ', true) == 0 && !target.equals(StringUtils.remove(line, " "))) {
                end = i - 1;
                break;
            }

            end = i;
        }

        return new int[]{start, end + 1};
    }

    public static List<String> getNewestConfigInput() {
        return NEWEST_CONFIG_INPUT;
    }
}
