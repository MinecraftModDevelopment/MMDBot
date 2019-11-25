package com.mcmoddev.bot.events;

import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 */
public final class MiscEvents extends ListenerAdapter {

	/**
	 *
	 */
    @Override
    public void onReady(final ReadyEvent event) {
        MMDBot.LOGGER.info("Bot is online and ready.");
    }

    /**
     *
     */
    @Override
    public void onDisconnect(final DisconnectEvent event) {
        MMDBot.LOGGER.warn("*** Connection terminated ***");
    }

    /**
     *
     */
    @Override
    public void onReconnect(final ReconnectedEvent event) {
        MMDBot.LOGGER.warn("*** Bot reconnected to Discord successfully. ***");
    }
}
