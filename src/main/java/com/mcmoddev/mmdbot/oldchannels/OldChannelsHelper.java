package com.mcmoddev.mmdbot.oldchannels;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class OldChannelsHelper {

	private static final Map<TextChannel, Long> channelLastMessageMap = new HashMap<>();

	private static boolean ready = false;

	public static long getLastMessageTime(final TextChannel channel) {
		return channelLastMessageMap.get(channel);
	}

	public static void clear() {
		channelLastMessageMap.clear();
		setReady(false);
	}

	public static void put(final TextChannel channel, final long timeSinceLastMessage) {
		channelLastMessageMap.put(channel, timeSinceLastMessage);
	}

	public static boolean isReady() {
		return ready;
	}

	public static void setReady(boolean ready) {
		OldChannelsHelper.ready = ready;
	}
}
