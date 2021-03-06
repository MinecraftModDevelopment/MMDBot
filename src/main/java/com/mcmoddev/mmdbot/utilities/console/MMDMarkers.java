package com.mcmoddev.mmdbot.utilities.console;

import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdMute;
import com.mcmoddev.mmdbot.modules.commands.server.moderation.CmdUnmute;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.fabric.FabricApiUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.forge.ForgeUpdateNotifier;
import com.mcmoddev.mmdbot.utilities.updatenotifiers.minecraft.MinecraftUpdateNotifier;
import com.mcmoddev.mmdbot.modules.logging.misc.EventReactionAdded;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Class for holding the {@link Marker}s used for logging.
 *
 * @author sciwhiz12
 */
public final class MMDMarkers {

    /**
     * The {@link Marker} for the {@link FabricApiUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FABRIC = MarkerFactory.getMarker("Notifier.Fabric");
    /**
     * The {@link Marker} for the {@link ForgeUpdateNotifier}.
     */
    public static final Marker NOTIFIER_FORGE = MarkerFactory.getMarker("Notifier.Forge");
    /**
     * The {@link Marker} for the {@link MinecraftUpdateNotifier}.
     */
    public static final Marker NOTIFIER_MC = MarkerFactory.getMarker("Notifier.MC");

    /**
     * The {@link Marker} for the requests removal system.
     *
     * @see EventReactionAdded
     */
    public static final Marker REQUESTS = MarkerFactory.getMarker("Requests");

    /**
     * The {@link Marker} for different guild-related events, such as role addition/removal or nickname changes.
     */
    public static final Marker EVENTS = MarkerFactory.getMarker("Events");

    /**
     * The {@link Marker} for the muting system.
     *
     * @see CmdMute
     * @see CmdUnmute
     */
    public static final Marker MUTING = MarkerFactory.getMarker("Muting");

    /**
     * Instantiates a new Mmd markers.
     */
    private MMDMarkers() {
        throw new IllegalStateException("Utility class");
    }
}
