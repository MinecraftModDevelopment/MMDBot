package com.mcmoddev.bot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Configuration {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Expose
    private String discordToken = "Enter your token!";

    @Expose
    private String commandKey = "!key";

    public String getDiscordToken () {

        return this.discordToken;
    }

    public String getCommandKey () {

        return this.commandKey;
    }

    public void setDiscordToken (String discordToken) {
        
        this.discordToken = discordToken;
    }

    public void setCommandKey (String commandKey) {
        
        this.commandKey = commandKey;
    }

    public static Configuration getConfig () {

        final File file = new File("config/config.json");

        // Read the config if it exists
        if (file.exists()) {

            MMDBot.LOG.info("Reading existing configuration file!");
            try (Reader reader = new FileReader(file)) {

                return GSON.fromJson(reader, Configuration.class);
            }

            catch (final IOException e) {

                MMDBot.LOG.trace("Failed to read config file.", e);
            }
        }

        // Otherwise make a new config file
        else {

            try (FileWriter writer = new FileWriter(file)) {

                GSON.toJson(new Configuration(), writer);
            }

            catch (final IOException e) {

                MMDBot.LOG.trace("Failed to write config file.", e);
            }

            MMDBot.LOG.error("New Configuration file generated!");
            MMDBot.LOG.error("Please modify the config and launch again.");
            System.exit(0);
        }

        // dead code
        return null;
    }
}