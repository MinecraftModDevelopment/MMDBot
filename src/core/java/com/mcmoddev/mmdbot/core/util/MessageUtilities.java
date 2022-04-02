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
package com.mcmoddev.mmdbot.core.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class MessageUtilities {

    public static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord.com/channels/");

    /**
     * Disables all the buttons that a message has. Disabling buttons deems it as not clickable to
     * the user who sees it.
     * <p>
     * This method already queues the changes for you and does not block in any way.
     *
     * @param message the message that contains at least one button
     * @throws IllegalArgumentException when the given message does not contain any action row
     */
    public static void disableButtons(@NonNull Message message) {
        if (message.getActionRows().isEmpty()) {
            throw new IllegalArgumentException("Message must contain at least one action row!");
        }
        final List<ActionRow> newRows = new ArrayList<>(message.getActionRows().size());
        for (final var row : message.getActionRows()) {
            newRows.add(ActionRow.of(row.getComponents().stream().map(item -> item instanceof Button button ? button.asDisabled() : item).toList()));
        }

        message
            .editMessageComponents(newRows)
            .queue();
    }

    public static void decodeMessageLink(final String link, MessageInfo consumer)
        throws MessageLinkException {
        final var matcher = MESSAGE_LINK_PATTERN.matcher(link);
        if (matcher.find()) {
            try {
                var originalWithoutLink = matcher.replaceAll("");
                if (originalWithoutLink.indexOf('/') > -1) {
                    final long guildId = Long
                        .parseLong(originalWithoutLink.substring(0, originalWithoutLink.indexOf('/')));
                    originalWithoutLink = originalWithoutLink.substring(originalWithoutLink.indexOf('/') + 1);
                    if (originalWithoutLink.indexOf('/') > -1) {
                        final long channelId = Long
                            .parseLong(originalWithoutLink.substring(0, originalWithoutLink.indexOf('/')));
                        originalWithoutLink = originalWithoutLink.substring(originalWithoutLink.indexOf('/') + 1);
                        final long messageId = Long.parseLong(originalWithoutLink);
                        consumer.accept(guildId, channelId, messageId);
                    } else {
                        throw new MessageLinkException("Invalid Link");
                    }
                } else {
                    throw new MessageLinkException("Invalid Link");
                }
            } catch (NumberFormatException e) {
                throw new MessageLinkException(e);
            }
        } else {
            throw new MessageLinkException("Invalid Link");
        }
    }

    public static class MessageLinkException extends Exception {

        @Serial
        private static final long serialVersionUID = -2805786147679905681L;

        public MessageLinkException(Throwable e) {
            super(e);
        }

        public MessageLinkException(String message) {
            super(message);
        }

    }

    @FunctionalInterface
    public interface MessageInfo {

        void accept(final long guildId, final long channelId, final long messageId);

    }

}
