package org.vivecraft.client.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.server.config.ServerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateChecker {

    public static boolean hasUpdate = false;

    public static String changelog = "";

    public static String newestVersion = "";

    public static boolean checkForUpdates() {
        // Disable auto-update
        return hasUpdate;
    }

    private static String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.joining("\n"));
    }

    private static class Version implements Comparable<Version> {

        public String fullVersion;

        public String changelog;

        public int major;
        public int minor;
        public int patch;
        int alpha = 0;
        int beta = 0;
        boolean featureTest = false;

        public Version(String version, String version_number, String changelog) {
            this.fullVersion = version;
            this.changelog = changelog;
            String[] parts = version_number.split("-");
            int viveVersionIndex = parts.length - 2;
            // parts should be [mc version]-(pre/rc)-[vive version]-(vive a/b/test)-[mod loader]
            if (!parts[viveVersionIndex].contains(".")) {
                viveVersionIndex = parts.length - 3;
                String testString = parts[parts.length - 2];
                // prerelease
                if (testString.matches("a\\d+.*")) {
                    alpha = Integer.parseInt(testString.replaceAll("\\D+", ""));
                } else if (testString.matches("b\\d+.*")) {
                    beta = Integer.parseInt(testString.replaceAll("\\D+", ""));
                }
                // if the prerelease string is not just aXX or bXX it's a feature test as well and ranked slightly higher
                if (!testString.replaceAll("^[ab]\\d+", "").isEmpty()) {
                    featureTest = true;
                }
            }
            String[] ints = parts[viveVersionIndex].split("\\.");
            // remove all letters, since stupid me put a letter in one version
            major = Integer.parseInt(ints[0].replaceAll("\\D+", ""));
            minor = Integer.parseInt(ints[1].replaceAll("\\D+", ""));
            patch = Integer.parseInt(ints[2].replaceAll("\\D+", ""));
        }

        @Override
        public int compareTo(UpdateChecker.Version o) {
            long result = this.compareNumber() - o.compareNumber();
            if (result < 0) {
                return 1;
            } else if (result == 0L) {
                return 0;
            }
            return -1;
        }

        public boolean isVersionType(char versionType) {
            return switch (versionType) {
                case 'r' -> beta == 0 && alpha == 0 && !featureTest;
                case 'b' -> beta >= 0 && alpha == 0 && !featureTest;
                case 'a' -> alpha >= 0 && !featureTest;
                default -> false;
            };
        }

        // two digits per segment, should be enough right?
        private long compareNumber() {
            // digit flag
            // major minor patch full release beta alpha feature test
            // 00    00    00    0            00   00    0
            return (featureTest ? 1L : 0L) +
                alpha * 10L +
                beta * 1000L +
                (alpha + beta == 0 ? 10000L : 0L) +
                patch * 1000000L +
                minor * 100000000L +
                major * 10000000000L;
        }
    }
}
