package com.mcmoddev.bot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * THIS IS A NOTE FOR DARKHAX. DO NOT MAKE THESE FIELDS FINAL. I KNOW YOU WANT TO, BUT IT WILL
 * BREAK EVERYTHING!
 */
public class Configuration {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONF_FILE = new File("data/config.json");

    @Expose
    private String discordToken = "Enter your token!";

    @Expose
    private String commandKey = "!key";

    @Expose
    private String encryptionKey = "Change This!";

    public String getDiscordToken () {

        return this.discordToken;
    }

    public String getCommandKey () {

        return this.commandKey;
    }

    public String getEncryptionKey () {

        return this.encryptionKey;
    }

    public static void saveConfig (Configuration config) {

        try (FileWriter writer = new FileWriter(CONF_FILE)) {

            GSON.toJson(config, writer);
        }

        catch (final IOException e) {

            MMDBot.LOG.trace("Failed to write config file.", e);
        }
    }

    @Override
    public String toString () {

        return "Configuration [discordToken=" + this.discordToken + ", commandKey=" + this.commandKey + ", encryptionKey=" + this.encryptionKey + "]";
    }

    public static Configuration getConfig () {

        // Read the config if it exists
        if (CONF_FILE.exists()) {

            MMDBot.LOG.info("Reading existing configuration file!");
            try (Reader reader = new FileReader(CONF_FILE)) {

                final Configuration config = GSON.fromJson(reader, Configuration.class);
                Configuration.saveConfig(config);
                return config;
            }

            catch (final IOException e) {

                MMDBot.LOG.trace("Failed to read config file.", e);
            }
        }

        // Otherwise make a new config file
        else {

            Configuration.saveConfig(new Configuration());
            MMDBot.LOG.error("New Configuration file generated!");
            MMDBot.LOG.error("Please modify the config and launch again.");
            System.exit(0);
        }

        // dead code
        return null;
    }

    public void setDiscordToken (String discordToken) {

        this.discordToken = discordToken;
    }

    public void setCommandKey (String commandKey) {

        this.commandKey = commandKey;
    }

    public void setEncryptionKey (String encryptionKey) {

        this.encryptionKey = encryptionKey;
    }
}