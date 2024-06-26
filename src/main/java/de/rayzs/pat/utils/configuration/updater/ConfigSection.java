package de.rayzs.pat.utils.configuration.updater;

import de.rayzs.pat.plugin.logger.Logger;
import java.nio.file.Files;
import java.io.File;
import java.util.*;

public class ConfigSection {

    private final File oldConfigFile;

    public ConfigSection(File oldConfigFile) {
        this.oldConfigFile = oldConfigFile;
    }

    public List<String> createAndGetNewFileInput(int interceptLine, int from, int to) {
        List<String> interceptedInput = gatherLines(from, to),
                     originalFileInput = new ArrayList<>();

        try {
            originalFileInput = Files.readAllLines(oldConfigFile.toPath());

            while(!originalFileInput.get(interceptLine).isEmpty()) interceptLine++;
            originalFileInput.addAll(interceptLine, interceptedInput);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return originalFileInput;
    }

    public List<String> gatherLines(int from, int to) {
        List<String> lines, sectionLines = new ArrayList<>();

        try {

            lines = ConfigUpdater.getNewestConfigInput();
            String targetString = lines.get(from);
            boolean foundLine = false;
            int line = from;

            for (String currentLine : lines) {

                if(!foundLine && targetString.equals(currentLine))
                    foundLine = true;

                if(!foundLine) continue;

                line++;
                if(line > to) break;

                sectionLines.add(currentLine);
            }

        } catch (Exception exception) {
            Logger.warning("Failed to read file input!");
            exception.printStackTrace();
        }

        return sectionLines;
    }
}
