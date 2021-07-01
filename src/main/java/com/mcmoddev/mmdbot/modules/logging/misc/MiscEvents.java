package com.mcmoddev.mmdbot.modules.logging.misc;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.core.TaskScheduler;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * The type Misc events.
 *
 * @author ProxyNeko
 */
public final class MiscEvents extends ListenerAdapter {

    /**
     * On ready.
     *
     * @param event the event
     */
    @Override
    public void onReady(final @NotNull ReadyEvent event) {
        MMDBot.LOGGER.warn("Bot is online and ready.");
        TaskScheduler.init();
        References.STARTUP_TIME = Instant.now();
    }

    /**
     * On disconnect.
     *
     * @param event the event
     */
    @Override
    public void onDisconnect(final @NotNull DisconnectEvent event) {
        MMDBot.LOGGER.warn("*** Connection to Discord terminated ***");
    }

    /**
     * On resumed.
     *
     * @param event the event
     */
    @Override
    public void onResumed(final @NotNull ResumedEvent event) {
        Utils.sleepTimer();
        MMDBot.LOGGER.warn("*** Bot reconnected to Discord successfully. ***");
    }
}
