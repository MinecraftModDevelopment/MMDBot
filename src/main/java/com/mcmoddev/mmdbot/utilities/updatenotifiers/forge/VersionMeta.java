package com.mcmoddev.mmdbot.utilities.updatenotifiers.forge;

/**
 * The type Version meta.
 *
 * @author Antoine Gagnon
 */
public final class VersionMeta {

    /**
     * The Version.
     */
    private final String version;

    /**
     * The State.
     */
    private final String state;

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Instantiates a new Version meta.
     *
     * @param versionIn the version in
     * @param stateIn   the state in
     */
    public VersionMeta(final String versionIn, final String stateIn) {
        this.version = versionIn;
        this.state = stateIn;
    }
}
