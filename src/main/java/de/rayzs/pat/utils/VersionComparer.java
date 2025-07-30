package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;

import java.util.Arrays;
import java.util.List;

public class VersionComparer {

    public enum VersionState { NEWER, UPDATED, OUTDATED }

    private static final VersionComparer instance = new VersionComparer();

    public static VersionComparer get() {
        return instance;
    }

    private VersionComparer() {}

    private final List<String> ignoreVersions = Arrays.asList("1.9.1", "1.9.2");
    private final String versionUrl = "https://www.rayzs.de/proantitab/api/version.php";

    private final int versionLength = 3;

    private Version currentVersion, newestVersion;
    private VersionState versionState = VersionState.UPDATED;

    private boolean shouldAnnounce = true;


    public boolean computeComparison() {
        String result = new ConnectionBuilder().setUrl(versionUrl)
                .setProperties("ProAntiTab", "4454")
                .connect()
                .getResponse();

        if (result == null)
            result = "/";

        if (result.equals("/")) {
            Logger.warning("Failed to connect to plugin page! Version comparison cannot be made. (No internet?)");
            return false;
        }

        Storage.NEWER_VERSION = result;
        VersionComparer.get().setNewestVersion(Storage.NEWER_VERSION);

        if (VersionComparer.get().isNewer()) {
            if (shouldAnnounce) {
                shouldAnnounce = false;

                Logger.info("§8[§fPAT | Bukkit§8] §7Please be aware that you are currently using a §bdeveloper §7version of ProAntiTab. Bugs, errors and a lot of debug messages might be included.");
            }

            return true;
        }

        if (VersionComparer.get().isUpdated()) {
            if (shouldAnnounce) {
                shouldAnnounce = false;

                Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines().forEach(Logger::warning);
            }

            return false;
        }

        if (VersionComparer.get().isOutdated()) {

            if (shouldAnnounce) {
                shouldAnnounce = false;

                Storage.OUTDATED = true;
                Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines().forEach(Logger::warning);
            }

            return true;
        }

        return false;
    }

    public void setCurrentVersion(String version) {
        if (version == null)
            return;

        currentVersion = new Version(version);
    }

    public void setNewestVersion(String version) {
        if (version == null)
            return;

        if (ignoreVersions.contains(version))
            return;

        newestVersion = new Version(version);
        compute();
    }

    public VersionState getVersionState() {
        return versionState;
    }

    public boolean isUpdated() {
        return versionState == VersionState.UPDATED;
    }

    public boolean isOutdated() {
        return versionState == VersionState.OUTDATED;
    }

    public boolean isNewer() {
        return versionState == VersionState.NEWER;
    }

    private void compute() {
        if (currentVersion == null || newestVersion == null)
            return;

        int[] crntNum = currentVersion.getVersionNums();
        int[] newNum = newestVersion.getVersionNums();

        for (int i = 0; i < versionLength; i++) {

            if (crntNum[i] > newNum[i]) {
                versionState = VersionState.NEWER;
                return;
            }

            if (crntNum[i] < newNum[i]) {
                versionState = VersionState.OUTDATED;
                return;
            }

        }
    }

    private class Version {

        private final int[] versionNums = new int[versionLength];
        private final String versionString;


        public Version(String versionString) {
            this.versionString = versionString;
            setVersionNums();
        }

        public String getVersionName() {
            return versionString;
        }

        public int[] getVersionNums() {
            return versionNums;
        }

        private void setVersionNums() {

            if (!versionString.contains("."))
                return;

            String[] details = versionString.split("\\.");
            for (int i = 0; i < details.length; i++) {
                if (i >= versionLength)
                    break;

                String detailString = details[i];

                if (!NumberUtils.isDigit(detailString))
                    continue;

                int detail = Integer.parseInt(detailString);
                versionNums[i] = detail;
            }
        }
    }
}
