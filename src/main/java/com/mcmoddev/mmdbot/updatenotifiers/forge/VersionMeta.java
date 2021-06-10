package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 *
 * @author
 *
 */
public final class VersionMeta {

	/**
	 *
	 */
    public String version;

    /**
     *
     */
    public String state;

    /**
     *
     * @param version
     * @param state
     */
    public VersionMeta(final String versionIn, final String stateIn) {
        this.version = versionIn;
        this.state = stateIn;
    }
}
