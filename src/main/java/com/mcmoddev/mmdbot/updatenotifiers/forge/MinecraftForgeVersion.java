package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 *
 * @author Antoine Gagnon
 *
 */
public final class MinecraftForgeVersion {

	/**
	 *
	 */
    private final ForgeVersion forgeVersion;
    /**
     *
     */
    private final String mcVersion;

    /**
     *
     * @param mcVersionIn
     * @param forgeVersionIn
     */
    public MinecraftForgeVersion(final String mcVersionIn, final ForgeVersion forgeVersionIn) {
        this.mcVersion = mcVersionIn;
        this.forgeVersion = forgeVersionIn;
    }

    /**
     *
     * @return ForgeVersion.
     */
    public ForgeVersion getForgeVersion() {
        return forgeVersion;
    }

    /**
     *
     * @return String.
     */
    public String getMcVersion() {
        return mcVersion;
    }
}
