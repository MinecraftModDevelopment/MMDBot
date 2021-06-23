package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 * @author Antoine Gagnon
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
     * @param versionIn
     * @param stateIn
     */
    public VersionMeta(final String versionIn, final String stateIn) {
        this.version = versionIn;
        this.state = stateIn;
    }
}
