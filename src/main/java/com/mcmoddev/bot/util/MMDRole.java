package com.mcmoddev.bot.util;

import com.mcmoddev.bot.MMDBot;

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

    /**
     * The unique id for the role on a server.
     */
    private final String roleId;

    /**
     * The unique id for the guild this role exists on.
     */
    private final String guildId;

    /**
     * An instance of the role as an IRole object.
     */
    private final IRole role;

    /**
     * An instance of the guild as an IGuild object.
     */
    private final IGuild guild;

    /**
     * Creates a representation of a role from Discord in code. This constructor will assume that
     * the role exists on the public MMD server.
     *
     * @param roleId The Id of the role to represent.
     */
    MMDRole (String roleId) {

        this(roleId, MMDBot.mmdGuild.getID());
    }

    /**
     * Creates a representation of a role from Discord in code.
     *
     * @param roleId The Id of the role to represent.
     * @param guildId The Id of the guild the role is on.
     */
    MMDRole (String roleId, String guildId) {

        this.roleId = roleId;
        this.guildId = guildId;
        this.guild = MMDBot.instance.getGuildByID(guildId);
        this.role = this.guild != null ? this.guild.getRoleByID(roleId) : null;
    }

    /**
     * Gets the id of the role being represented.
     *
     * @return The id of the role being represented.
     */
    public String getRoleId () {

        return this.roleId;
    }

    /**
     * Gets the id of the guild that the role exists on.
     *
     * @return The id of the guild that the role exists on.
     */
    public String getGuildId () {

        return this.guildId;
    }

    /**
     * Gets the IRole representation of the role.
     *
     * @return The IRole representation of the role.
     */
    public IRole getRole () {

        return this.role;
    }

    /**
     * Gets the IGuild representation of the guild the role exists on.
     *
     * @return The IGuild representation of the guild the role exists on.
     */
    public IGuild getGuild () {

        return this.guild;
    }

    /**
     * Checks if a user has the role.
     *
     * @param user The user to check permissions of.
     * @return Whether or not they have the role.
     */
    public boolean hasRole (IUser user) {

        if (this.guild != null)
            for (final IRole role : user.getRolesForGuild(this.guild))
                if (role.getID().equals(this.roleId))
                    return true;

        return false;
    }
}
