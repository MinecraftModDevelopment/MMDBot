package com.mcmoddev.bot.util;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public enum MMDRole {

	DISCORD_MAINTAINER("249828846738735114"),
    ADMIN("176781877682634752"),
    MODERATOR("178772974990655489"),
    SOCIAL("252881628538470401"),
    DEV_SERVER_ADMIN("191296278431399936"),
    MODDER_MMD("218607518048452610"),
    DEV_SERVER("219741912695832576"),
    BOT("178773609521741825"),
    BOT_HOST("226067502977777664"),
    STREAMER("219679192462131210");
	
	private final String id;
	
	MMDRole(String id) {
		
		this.id = id;
	}

	public String getId() {
		
		return id;
	}
	
	public IRole getRole(IGuild guild) {
		
		return guild.getRoleByID(this.getId());
	}
	
	public boolean hasRole(IUser user, IGuild guild) {
		
		for (IRole role : user.getRolesForGuild(guild))
			if (role.getID().equals(this.getId()))
				return true;
		
		return false;
	}
}
