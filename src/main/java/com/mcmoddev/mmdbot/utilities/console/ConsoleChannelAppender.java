package com.mcmoddev.mmdbot.utilities.console;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.BotConfig;
import net.dv8tion.jda.api.MessageBuilder;

import java.util.Collections;

/**
 * A custom {@link ch.qos.logback.core.Appender} for logging to a Discord channel.
 * <p>
 * This appender may have an inner {@link Layout}, for formatting the message to be sent to the channel.
 * Otherwise, {@link ILoggingEvent#getFormattedMessage()} will be sent.
 *
 * @author sciwhiz12
 * @see ConsoleChannelLayout
 */
public class ConsoleChannelAppender extends AppenderBase<ILoggingEvent> {

    /**
     * The Allow mentions.
     */
    private boolean allowMentions;

    /**
     * The Layout.
     */
    private Layout<ILoggingEvent> layout;

    /**
     * Sets whether the Discord messages should allow mentions, i.e. ping any mentioned users and roles.
     *
     * @param allowMentionsIn Whether to allow mentions
     */
    public void setAllowMentions(final boolean allowMentionsIn) {
        this.allowMentions = allowMentionsIn;
    }

    /**
     * Sets the inner {@link Layout}, used for formatting the message to be sent.
     *
     * @param layoutIn The layout
     */
    public void setLayout(final Layout<ILoggingEvent> layoutIn) {
        this.layout = layoutIn;
    }

    /**
     * {@inheritDoc}
     *
     * @param event the event
     */
    @Override
    protected void append(final ILoggingEvent event) {
        final var jda = MMDBot.getInstance();
        final BotConfig config = MMDBot.getConfig();
        if (jda != null && config != null) {
            final var guild = jda.getGuildById(config.getGuildID());
            if (guild == null) {
                return;
            }
            final var channel = guild.getTextChannelById(config.getChannel("console"));
            if (channel == null) {
                return;
            }
            final var builder = new MessageBuilder();
            builder.append(layout != null ? layout.doLayout(event) : event.getFormattedMessage());
            if (!allowMentions) {
                builder.setAllowedMentions(Collections.emptyList());
            }
            channel.sendMessage(builder.build()).queue();
        }
    }
}
