package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.*;
import java.nio.file.Files;
import java.util.*;
import java.io.*;

public class ConfigUpdater {

    private static List<String> NEWEST_CONFIG_INPUT = new ArrayList<>();

    public static void initialize() {
        NEWEST_CONFIG_INPUT = new ConnectionBuilder().setUrl("https://raw.githubusercontent.com/RayzsYT/ProAntiTab/main/src/main/resources/files/"
                + (Reflection.isProxyServer() ? " proxy" : "bukkit")
                + "-config.yml").connect().getResponseList();
    }

    public static void updateConfigFile(File file, String target, boolean section) {
        int[] position = section ? getPositionBySection(NEWEST_CONFIG_INPUT, target, true) : getSectionPositionByTarget(NEWEST_CONFIG_INPUT, target, true);
        updateConfigFile(file, position[0], position[0], position[1]);
    }

    public static void updateConfigFile(File file, int atLine, int from, int to) {
        ConfigSection configSection = new ConfigSection(file);

        try {
            Files.write(file.toPath(), configSection.createAndGetNewFileInput(atLine, from, to));
        }catch (Exception exception) {
            Logger.warning("Failed to read file input! (#2)");
            exception.printStackTrace();
        }
    }

    public static String getSection(String target) {
        if(target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            target = pathSplit[pathSplit.length-1];
        }

        return target;
    }

    public static int[] getSectionPositionByTarget(List<String> lines, String targetPath, boolean comments) {
        HashMap<Integer, String> hash = new HashMap<>();
        int sections = 0;

        String target = targetPath;
        if(target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            sections = (pathSplit.length-1) * 2;

            target = pathSplit[pathSplit.length-1];
        }

        String line;
        int i;
        for (i = 0; i < lines.size(); i++) {
            line = lines.get(i);

            if(line.isEmpty() || line.startsWith("#") || !line.contains(":")) continue;
            line = line.split(":")[0];

            if(!StringUtils.remove(line, " ").equals(target)) continue;
            if(StringUtils.countLetters(line, ' ', true) != sections) continue;
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

                if(spaces == sections) {
                    sections -= removeSpaces;
                    line = StringUtils.remove((line != null && line.contains(":") ? line.split(":")[0] : line), " ");
                    finalStartPos = position;
                    sectionPath.insert(0, line + ".");
                }

                if(position < 0) break;
                position--;
            } while (spaces != 0);

            if(sectionPath.toString().equals(targetPath)) {
                finalStartPos = targetPath.contains(".") ? finalStartPos : finalEndPos;

                if(comments) {
                    int start = finalStartPos-1;
                    for(i = start; i > 0; i--) {
                        line = StringUtils.getLineText(lines, i);
                        if(line != null && !line.startsWith("#")) {
                            finalStartPos = i+1;
                            break;
                        }
                    }
                }

                return new int[] { finalStartPos, finalEndPos + 1 };
            }
        }

        return new int[] {0, 0};
    }

    public static int[] getPositionBySection(List<String> lines, String target, boolean comments) {
        int start = getSectionPositionByTarget(lines, target, comments)[0], end = start, i;
        String line;

        if(target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            target = pathSplit[0];
        }

        boolean reachedSection = false;

        for (i = start; i < lines.size(); i++) {
            line = lines.get(i);

            if(line.contains(":")) line = line.split(":")[0];
            if(!reachedSection && target.equals(StringUtils.remove(line, " ")))
                reachedSection = true;

            if(line == null) continue;
            System.out.println(target + "/" + line);

            if(line.startsWith("#")) {
                if(!reachedSection) continue;
                end = i-1;
                break;
            }

            if(StringUtils.countLetters(line, ' ', true) == 0 && !target.equals(StringUtils.remove(line, " "))) {
                end = i-1;
                break;
            }

            end = i;
        }

        return new int[] {start, end + 1};
    }

    public static List<String> getNewestConfigInput() {
        return NEWEST_CONFIG_INPUT;
    }
}
