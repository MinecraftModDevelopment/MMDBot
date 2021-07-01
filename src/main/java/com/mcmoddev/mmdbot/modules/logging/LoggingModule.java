package com.mcmoddev.mmdbot.modules.logging;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.modules.logging.users.EventNicknameChanged;
import com.mcmoddev.mmdbot.modules.logging.users.EventRoleAdded;
import com.mcmoddev.mmdbot.modules.logging.users.EventRoleRemoved;
import com.mcmoddev.mmdbot.modules.logging.users.EventUserJoined;
import com.mcmoddev.mmdbot.modules.logging.users.EventUserLeft;

/**
 * Splits off event logging so we can disable it if the API ever breaks or if we are in dev,
 * this way we can avoid spamming errors or duplicate logging of events.
 *
 * @author ProxyNeko
 */
public class LoggingModule {

    /**
     * Setup and load the bots logging module.
     */
    public static void setupLoggingModule() {
        if (MMDBot.getConfig().isEventLoggingModuleEnabled()) {
            MMDBot.getInstance()
                .addEventListener(
                    new EventUserJoined(),
                    new EventUserLeft(),
                    new EventNicknameChanged(),
                    new EventRoleAdded(),
                    new EventRoleRemoved(),
                    new EventReactionAdded());
            MMDBot.LOGGER.warn("Event logging module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Event logging module disabled via config, Discord event logging won't work right now!");
        }
    }
}
