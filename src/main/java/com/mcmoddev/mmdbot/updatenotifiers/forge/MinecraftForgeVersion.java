package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 *
 * @author
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
     * @return
     */
    public ForgeVersion getForgeVersion() {
        return forgeVersion;
    }

    /**
     *
     * @return
     */
    public String getMcVersion() {
        return mcVersion;
    }
}
