package com.mcmoddev.bot.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmoddev.bot.MMDBot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class BotConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG = new File("mmdbot_config.json");

    //General info.
    private String botToken = "Enter your bot token here.";
    private String prefix = "Command prefix";
    private String botTextStatus = "Can be \'I'm watching you\' or something similar but not too long.";
    private Long guildID = 0L;
    private Long botStuffChannelId = 0L;

    //Channel ID's
    private Long channelIDBasicEvents = 0L;
    private Long channelIDImportantEvents = 0L;
    private Long channelIDDeletedMessages = 0L;
    private Long channelIDDebug = 0L;
    private Long channelIDConsole = 0L;

    public String getBotToken() {
        return botToken;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getBotTextStatus() {
        return botTextStatus;
    }

    public Long getGuildID() {
        return guildID;
    }

    public Long getBotStuffChannelId() {
        return botStuffChannelId;
    }

    public Long getChannelIDBasicEvents() {
        return channelIDBasicEvents;
    }

    public Long getChannelIDImportantEvents() {
        return channelIDImportantEvents;
    }

    public Long getChannelIDDeletedMessages() {
        return channelIDDeletedMessages;
    }

    public Long getChannelIDDebug() {
        return channelIDDebug;
    }

    public Long getChannelIDConsole() {
        return channelIDConsole;
    }

    private static void setConfig(BotConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            MMDBot.LOGGER.error("An IOException occurred...", exception);
        }
    }

    public static BotConfig getConfig() {
        if (CONFIG.exists()) {
            try (Reader reader = new FileReader(CONFIG)) {
                BotConfig config = GSON.fromJson(reader, BotConfig.class);

                BotConfig.setConfig(config);
                return config;
            } catch (IOException exception) {
                MMDBot.LOGGER.trace("Failed to read config...", exception);
            }
        } else {
            BotConfig.setConfig(new BotConfig());
            MMDBot.LOGGER.error("New Config generated please edit and add a bot token to the config and start the bot.");
            System.exit(0);
        }
        return null;
    }
}
