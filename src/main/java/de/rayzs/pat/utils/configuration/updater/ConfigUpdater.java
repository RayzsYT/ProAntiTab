package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
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

    public static void updateConfigFile(ConfigurationBuilder configurationBuilder, int atLine, int from, int to) {
        File file = configurationBuilder.getFile();
        ConfigSection configSection = new ConfigSection(file);
        String sectionAsString = StringUtils.buildStringListWithoutColors(configSection.createAndGetNewFileInput(atLine, from, to));

        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, false));
            writer.write(sectionAsString);
            writer.close();
        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static int[] getSectionPositionByTarget(ConfigurationBuilder configurationBuilder, String targetPath, boolean comments) {
        List<String> lines = new LinkedList<>();
        HashMap<Integer, String> hash = new HashMap<>();
        int sections = 0;

        String target = targetPath;
        if(target.contains(".")) {
            String[] pathSplit = target.split("\\.");
            sections = (pathSplit.length-1) * 2;

            target = pathSplit[pathSplit.length-1];
        }

        try {
            lines = Files.readAllLines(configurationBuilder.getFile().toPath());
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
        } catch (Exception exception) {
            exception.printStackTrace();
            return new int[] {0, 0};
        }

        String line;
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
                    int i;
                    for(i = finalStartPos; i > 0; i--) {
                        line = StringUtils.getLineText(lines, i);
                        if(line != null && !line.startsWith("#")) {
                            finalStartPos = i;
                            break;
                        }
                    }
                }

                return new int[] { finalStartPos, finalEndPos };
            }
        }

        return new int[] {0, 0};
    }

    public static List<String> getNewestConfigInput() {
        return NEWEST_CONFIG_INPUT;
    }
}
