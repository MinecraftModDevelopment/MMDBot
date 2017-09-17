package com.mcmoddev.bot.handlers;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class StateHandler {

    private boolean isReady = false;

    private boolean isProduction = false;

    // Public
    private IGuild guildPublic;

    private IChannel chanelDebug;

    private IChannel chanelAudit;

    private IChannel chanelConsole;

    private IChannel channelNewCurse;

    private IRole roleAdmin;

    private IRole roleBotManager;

    private IRole roleModerator;

    private IRole roleMuted;

    // Staff
    private IGuild guildStaff;

    @EventSubscriber
    public void onReady (ReadyEvent event) {

        this.isProduction = MMDBot.instance.getOurUser().getLongID() == 271222230438903812L;

        // Public
        this.guildPublic = MMDBot.instance.getGuildByID(176780432371744769L);
        this.chanelDebug = MMDBot.instance.getChannelByID(179302857143615489L);
        this.chanelAudit = MMDBot.instance.getChannelByID(271498021286576128L);
        this.channelNewCurse = MMDBot.instance.getChannelByID(358089884692643852L);
        this.chanelConsole = MMDBot.instance.getChannelByID(356312255270486027L);
        this.roleAdmin = this.guildPublic.getRoleByID(176781877682634752L);
        this.roleBotManager = this.guildPublic.getRoleByID(226067502977777664L);
        this.roleModerator = this.guildPublic.getRoleByID(178772974990655489L);
        this.roleMuted = this.guildPublic.getRoleByID(305875306529554432L);

        // Staff
        this.guildStaff = MMDBot.instance.getGuildByID(229851088319283202L);

        this.isReady = true;

        MMDBot.LOG.info("Logged in as " + Utilities.userString(MMDBot.instance.getOurUser()));
    }

    public boolean isReady () {

        return this.isReady;
    }

    public boolean isProductionBot () {

        return this.isProduction;
    }

    public IGuild getPublicGuild () {

        return this.guildPublic;
    }

    public IChannel getDebugChannel () {

        return this.chanelDebug;
    }

    public IChannel getAuditChannel () {

        return this.chanelAudit;
    }

    public IChannel getConsoleChannel () {

        return this.chanelConsole;
    }

    public IChannel getCurseChannel () {

        return this.channelNewCurse;
    }

    public IGuild getStaffGuild () {

        return this.guildStaff;
    }

    public boolean isAdmin (IUser user) {

        return Utilities.hasRole(user, this.roleAdmin);
    }

    public boolean isModerator (IUser user) {

        return Utilities.hasRole(user, this.roleModerator);
    }

    public boolean isBotManager (IUser user) {

        return Utilities.hasRole(user, this.roleBotManager);
    }

    public boolean isMuted (IUser user) {

        return Utilities.hasRole(user, this.roleMuted);
    }

    public boolean isPublicGuild (IGuild guild) {

        return this.guildPublic.getLongID() == guild.getLongID();
    }
}