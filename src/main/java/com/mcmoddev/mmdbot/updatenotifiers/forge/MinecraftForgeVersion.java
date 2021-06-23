package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 * The type Minecraft forge version.
 *
 * @author Antoine Gagnon
 */
public final class MinecraftForgeVersion {

    /**
     * The Forge version.
     */
    private final ForgeVersion forgeVersion;
    /**
     * The Mc version.
     */
    private final String mcVersion;

    /**
     * Instantiates a new Minecraft forge version.
     *
     * @param mcVersionIn    the mc version in
     * @param forgeVersionIn the forge version in
     */
    public MinecraftForgeVersion(final String mcVersionIn, final ForgeVersion forgeVersionIn) {
        this.mcVersion = mcVersionIn;
        this.forgeVersion = forgeVersionIn;
    }

    /**
     * Gets forge version.
     *
     * @return ForgeVersion. forge version
     */
    public ForgeVersion getForgeVersion() {
        return forgeVersion;
    }

    /**
     * Gets mc version.
     *
     * @return String. mc version
     */
    public String getMcVersion() {
        return mcVersion;
    }
}
