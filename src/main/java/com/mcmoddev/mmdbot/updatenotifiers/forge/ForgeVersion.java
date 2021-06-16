package com.mcmoddev.mmdbot.updatenotifiers.forge;

/**
 *
 * @author
 *
 */
public final class ForgeVersion {

	/**
	 *
	 */
    private String recommended;

    /**
     *
     */
    private String latest;

    /**
     *
     */
    public ForgeVersion() {
        this.recommended = "(unspecified)";
        this.latest = "(unspecified)";
    }

    /**
     *
     * @param recommendedIn
     * @param latestIn
     */
    public ForgeVersion(final String recommendedIn, final String latestIn) {
        this.recommended = recommendedIn;
        this.latest = latestIn;
    }

    /**
     *
     * @return String.
     */
    public String getRecommended() {
        return recommended;
    }

    /**
     *
     * @param recommendedIn
     */
    public void setRecommended(final String recommendedIn) {
        this.recommended = recommendedIn;
    }

    /**
     *
     * @return String.
     */
    public String getLatest() {
        return latest;
    }

    /**
     *
     * @param latestIn
     */
    public void setLatest(final String latestIn) {
        this.latest = latestIn;
    }
}
