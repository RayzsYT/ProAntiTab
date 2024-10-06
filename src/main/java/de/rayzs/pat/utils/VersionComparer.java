package de.rayzs.pat.utils;

import de.rayzs.pat.plugin.logger.Logger;

public class VersionComparer {

    private static Version CURRENT_VERSION, NEWEST_VERSION;

    public static void setCurrentVersion(String versionName) {
        if(CURRENT_VERSION != null && CURRENT_VERSION.getVersionName().equals(versionName)) return;
        CURRENT_VERSION = new Version(versionName);
    }

    public static void setNewestVersion(String versionName) {
        if(NEWEST_VERSION != null && NEWEST_VERSION.getVersionName().equals(versionName)) return;
        NEWEST_VERSION = new Version(versionName);
    }

    public static Version getCurrentVersion() {
        return CURRENT_VERSION;
    }

    public static Version getNewestVersion() {
        return NEWEST_VERSION;
    }

    public static boolean isNewest() {
        if(CURRENT_VERSION == null || NEWEST_VERSION == null) return true;

        return CURRENT_VERSION.isSame(NEWEST_VERSION);
    }

    public static boolean isUnreleased() {
        if(CURRENT_VERSION == null || NEWEST_VERSION == null) return true;

        return CURRENT_VERSION.isNewer(NEWEST_VERSION);
    }

    public static boolean isOutdated() {
        if(CURRENT_VERSION == null || NEWEST_VERSION == null) return false;
        return CURRENT_VERSION.isOlder(NEWEST_VERSION);
    }

    public static boolean isDeveloperVersion() {
        return CURRENT_VERSION.getState() == State.DEV;
    }

    public static class Version {
        private final String versionName;
        private int release = -1, major = -1, patch = -1;
        private State state = State.RELEASE;

        public Version(String version) {
            this.versionName = version;

            try {
                String[] versionSplit = version.split("\\.");
                release = Integer.parseInt(versionSplit[0]);
                major = Integer.parseInt(versionSplit[1]);

                String patchAsString = versionSplit[2];
                patchAsString = patchAsString.contains("-") ? patchAsString.split("-")[0] : patchAsString;

                patch = Integer.parseInt(patchAsString);

                if(!version.contains("-")) return;
                String stateName = version.split("-")[1].toUpperCase();
                state = State.valueOf(stateName);

            } catch (Throwable throwable) {
                Logger.warning("Failed to read version name! [" + version + "]");
                throwable.printStackTrace();
            }
        }

        public String getVersionName() {
            return versionName;
        }

        public boolean isSame(Version version) {
            return version.getVersionName().equals(versionName);
        }

        public boolean isNewer(Version comparedVersion) {
            return !isSame(comparedVersion) && !isOlder(comparedVersion);
        }

        public boolean isOlder(Version comparedVersion) {
            if(release < comparedVersion.getRelease()) return true;
            if(major < comparedVersion.getMajor()) return true;

            return patch < comparedVersion.getPatch();
        }

        public int getRelease() {
            return release;
        }

        public int getMajor() {
            return major;
        }

        public int getPatch() {
            return patch;
        }

        public State getState() {
            return state;
        }

        public boolean isInvalid() {
            return release == -1 || major == -1 || patch == -1;
        }
    }

    public enum State {
        RELEASE, DEV, ALPHA, BETA
    }
}
