/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.logging;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Splits off event logging, so we can disable it if the API ever breaks or if we are in dev,
 * this way we can avoid spamming errors or duplicate logging of events.
 *
 * @author KiriCattus
 */
public class LoggingModule {

    private static final Executor THREAD_POOL = Executors.newFixedThreadPool(2, r -> Utils.setThreadDaemon(new Thread(r, "LoggingListener"), true));

    /**
     * Setup and load the bots logging module.
     */
    public static void setupLoggingModule() {
        if (MMDBot.getConfig().isEventLoggingModuleEnabled()) {
            MMDBot.getJDA()
                .addEventListener(
                    loggingEvent(new EventReactionAdded()),
                    loggingEvent(new ScamDetector()));
            MMDBot.LOGGER.warn("Event logging module enabled and loaded.");
        } else {
            MMDBot.LOGGER.warn("Event logging module disabled via config, Discord event logging won't work right now!");
        }
    }

    private static EventListener loggingEvent(EventListener listener) {
        return new ThreadedEventListener(listener, THREAD_POOL);
    }

    public static void executeInLoggingChannel(LoggingType loggingType, Consumer<TextChannel> channel) {
        Utils.useTextChannel("events." + loggingType.toString(), channel);
    }

    public enum LoggingType {
        IMPORTANT("important"),
        REQUESTS_DELETION("requests_deletion");

        private final String string;

        LoggingType(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
