package com.mcmoddev.mmdbot.updatenotifiers.forge;

public class ForgeVersion {
	private String recommended;
	private String latest;

	public ForgeVersion() {
	}

	public ForgeVersion(String recommended, String latest) {
		this.recommended = recommended;
		this.latest = latest;
	}

	public String getRecommended() {
		return recommended;
	}

	public String getLatest() {
		return latest;
	}

	public void setRecommended(String recommended) {
		this.recommended = recommended;
	}

	public void setLatest(String latest) {
		this.latest = latest;
	}
}
