package com.mcmoddev.mmdbot.utilities.updatenotifiers.forge;

/**
 * The type Forge version.
 *
 * @author Antoine Gagnon
 */
public final class ForgeVersion {

    /**
     * The Recommended.
     */
    private String recommended;

    /**
     * The Latest.
     */
    private String latest;

    /**
     * Instantiates a new Forge version.
     */
    public ForgeVersion() {
        this.recommended = "(unspecified)";
        this.latest = "(unspecified)";
    }

    /**
     * Instantiates a new Forge version.
     *
     * @param recommendedIn the recommended in
     * @param latestIn      the latest in
     */
    public ForgeVersion(final String recommendedIn, final String latestIn) {
        this.recommended = recommendedIn;
        this.latest = latestIn;
    }

    /**
     * Gets recommended.
     *
     * @return String. recommended
     */
    public String getRecommended() {
        return recommended;
    }

    /**
     * Sets recommended.
     *
     * @param recommendedIn the recommended in
     */
    public void setRecommended(final String recommendedIn) {
        this.recommended = recommendedIn;
    }

    /**
     * Gets latest.
     *
     * @return String. latest
     */
    public String getLatest() {
        return latest;
    }

    /**
     * Sets latest.
     *
     * @param latestIn the latest in
     */
    public void setLatest(final String latestIn) {
        this.latest = latestIn;
    }
}
