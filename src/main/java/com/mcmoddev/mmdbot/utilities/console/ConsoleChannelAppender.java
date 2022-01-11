/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
