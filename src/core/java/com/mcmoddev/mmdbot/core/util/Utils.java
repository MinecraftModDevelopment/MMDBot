package com.mcmoddev.mmdbot.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public final class Utils {

    /**
     * @return the current public IP address of the machine.
     */
    public static String getPublicIPAddress() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (InputStreamReader sr = new InputStreamReader(url.openStream());
                 BufferedReader sc = new BufferedReader(sr)) {
                return sc.readLine().trim();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
