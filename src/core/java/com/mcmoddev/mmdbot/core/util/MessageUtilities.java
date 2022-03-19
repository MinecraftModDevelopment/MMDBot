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
