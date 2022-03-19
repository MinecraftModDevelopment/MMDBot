package com.mcmoddev.mmdbot.commander.util;

import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass
public class TheCommanderUtilities {

    /**
     * Gets a cat fact.
     *
     * @return a cat fact
     */
    public static String getCatFact() {
        try {
            final var url = new URL("https://catfact.ninja/fact");
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            final var reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            final String inputLine = reader.readLine();
            reader.close();
            final var objectArray = JsonParser.parseString(inputLine).getAsJsonObject();
            return ":cat:  " + objectArray.get("fact").toString();

        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            log.error("Error getting cat fact...", ex);
            ex.printStackTrace();
        }
        return "";
    }

}
