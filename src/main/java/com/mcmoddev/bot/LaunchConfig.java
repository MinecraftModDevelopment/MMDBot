package com.mcmoddev.bot;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LaunchConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static LaunchConfig config;

    public String authToken = "Enter your auth token!";
    public String key = "!mmd";

    public static LaunchConfig updateConfig() {

        if (config == null) {

            File file = new File("config.json");

            // Read the config if it exists
            if (file.exists()) {

                MMDBot.LOG.info("Reading existing configuration file!");
                try (Reader reader = new FileReader(file)) {

                    config = GSON.fromJson(reader, LaunchConfig.class);
                }

                catch (IOException e) {

                    MMDBot.LOG.trace("Failed to read launch config.", e);
                }
            }

            // Otherwise make a new config file
            else {

                MMDBot.LOG.info("Creating new config");
                config = new LaunchConfig();

                try (FileWriter writer = new FileWriter(file)) {

                    GSON.toJson(config, writer);
                }

                catch (IOException e) {

                    MMDBot.LOG.trace("Could not create config!", e);
                }

                MMDBot.LOG.error("Configuration file generated!");
                MMDBot.LOG.error("Please modify the config and launch again.");
                System.exit(0);
            }
        }
        return config;
    }
}
