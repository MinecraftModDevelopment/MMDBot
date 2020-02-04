package com.mcmoddev.bot.helpers.forge;

import org.jetbrains.annotations.NotNull;

public class SemVer implements Comparable<SemVer> {

	private Integer major;
	private Integer minor;
	private Integer patch;

	public SemVer(String versionString) {
		String[] vs = versionString.split("\\.");
		this.major = Integer.parseInt(vs[0]);
		this.minor = Integer.parseInt(vs[1]);

		if (vs.length == 3)
			this.patch = Integer.parseInt(vs[2]);
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public int compareTo(@NotNull SemVer other) {
		if (this.major > other.major) return 1;
		if (this.major < other.major) return -1;


		if (this.minor > other.minor) return 1;
		if (this.minor < other.minor) return -1;

		if (this.patch == null && other.patch != null) return -1;
		if (this.patch != null && other.patch == null) return 1;

		if (this.patch > other.patch) return 1;
		if (this.patch < other.patch) return -1;

		return 0;
	}

	@Override
	public String toString() {
		if (this.patch == null) {
			return String.format("%d.%d", major, minor);
		} else {
			return String.format("%d.%d.%d", major, minor, patch);
		}
	}
}
