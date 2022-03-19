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

import lombok.experimental.UtilityClass;

import java.io.Serial;
import java.util.regex.Pattern;

@UtilityClass
public class MessageUtilities {

    public static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord.com/channels/");

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
