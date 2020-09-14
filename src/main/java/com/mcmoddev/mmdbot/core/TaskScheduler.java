package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.updatenotifiers.game.MinecraftUpdateNotifier;

import java.util.Timer;

public class TaskScheduler {
	public static Timer timer = new Timer();

	public static void init() {
		try {
			// Check every 12 hours
			timer.scheduleAtFixedRate(new ForgeUpdateNotifier(), 0, 1000 * 60 * 60);
		} catch (Exception e) {
			MMDBot.LOGGER.error("Unable to schedule job Forge Update Notifier", e);
			e.printStackTrace();
		}
		// Check every 12 hours
		timer.scheduleAtFixedRate(new MinecraftUpdateNotifier(), 0, 1000 * 60 * 60);
		timer.scheduleAtFixedRate(new FabricApiUpdateNotifier(), 0, 1000 * 60 * 60);
	}
}
