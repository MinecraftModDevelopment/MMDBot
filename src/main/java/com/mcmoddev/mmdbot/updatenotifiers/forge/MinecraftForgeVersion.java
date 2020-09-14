package com.mcmoddev.mmdbot.updatenotifiers.forge;

public class MinecraftForgeVersion {
	ForgeVersion forgeVersion;
	String mcVersion;

	public MinecraftForgeVersion(String mcVersion, ForgeVersion forgeVersion) {
		this.mcVersion = mcVersion;
		this.forgeVersion = forgeVersion;
	}

	public ForgeVersion getForgeVersion() {
		return forgeVersion;
	}

	public String getMcVersion() {
		return mcVersion;
	}
}
