package com.mcmoddev.mmdbot.utilities.oldchannels;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Old channels helper.
 *
 * @author williambl The type Old channels helper.
 */
public class OldChannelsHelper {

    /**
     * The constant channelLastMessageMap.
     */
    private static final Map<TextChannel, Long> CHANNEL_LAST_MESSAGE_MAP = new HashMap<>();

    /**
     * The constant ready.
     */
    private static boolean ready = false;

    /**
     * Gets last message time.
     *
     * @param channel the channel
     * @return the last message time
     */
    public static long getLastMessageTime(final TextChannel channel) {
        return CHANNEL_LAST_MESSAGE_MAP.getOrDefault(channel, -1L);
    }

    /**
     * Clear.
     */
    public static void clear() {
        CHANNEL_LAST_MESSAGE_MAP.clear();
        setReady(false);
    }

    /**
     * Put.
     *
     * @param channel              the channel
     * @param timeSinceLastMessage the time since last message
     */
    public static void put(final TextChannel channel, final long timeSinceLastMessage) {
        CHANNEL_LAST_MESSAGE_MAP.put(channel, timeSinceLastMessage);
    }

    /**
     * Is ready boolean.
     *
     * @return the boolean
     */
    public static boolean isReady() {
        return ready;
    }

    /**
     * Sets ready.
     *
     * @param ready the ready
     */
    public static void setReady(final boolean ready) {
        OldChannelsHelper.ready = ready;
    }
}
