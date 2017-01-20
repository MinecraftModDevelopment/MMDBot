package com.mcmoddev.bot.handlers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.member.NickNameChangeEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserBanEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserPardonEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserRoleUpdateEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class ServerEventHandler {

    private static final int POSITIVE = Color.GREEN.getRGB();

    private static final int NEUTRAL = Color.YELLOW.getRGB();

    private static final int NEGATIVE = Color.RED.getRGB();

    @EventSubscriber
    public void onReady (ReadyEvent event) {

        MMDBot.mmdGuild = MMDBot.instance.getGuildByID("176780432371744769");
        MMDBot.botZone = MMDBot.instance.getChannelByID("179302857143615489");
        MMDBot.events = MMDBot.instance.getChannelByID("271498021286576128");
        Utilities.sendMessage(MMDBot.events, "MMDBot is up and ready!");
    }

    @EventSubscriber
    public void onUserJoin (UserJoinEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();

            embed.withDescription("**USER JOIN**");
            embed.appendField("**User**", Utilities.userString(user), false);
            embed.appendField("**Creation Date**", Utilities.formatTime(user.getCreationDate()), false);

            Utilities.sendMessage(MMDBot.events, embed, POSITIVE);
        }
    }

    @EventSubscriber
    public void onUserLeave (UserLeaveEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {
            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER LEAVE**");
            embed.appendField("**User**", Utilities.userString(user), true);
            embed.appendField("**Creation Date**", Utilities.formatTime(user.getCreationDate()), false);
            Utilities.sendMessage(MMDBot.events, embed, NEGATIVE);
        }
    }

    @EventSubscriber
    public void onUserRoles (UserRoleUpdateEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();

            final List<IRole> newRoles = event.getNewRoles();
            final List<IRole> oldRoles = event.getOldRoles();

            embed.withDescription("**USER ROLES CHANGED**");
            embed.appendField("**User**", Utilities.userString(user), false);
            embed.appendField("**Editor**", "API does not provide yet!", false);
            embed.appendField("**Current Roles**", Utilities.toString(newRoles, ", "), false);

            final List<IRole> addedRoles = new ArrayList<>(newRoles);
            addedRoles.removeAll(oldRoles);

            if (!addedRoles.isEmpty())
                embed.appendField("**Added Roles**", Utilities.toString(addedRoles, ", "), false);

            final List<IRole> removedRoles = new ArrayList<>(oldRoles);
            removedRoles.removeAll(newRoles);

            if (!removedRoles.isEmpty())
                embed.appendField("**Removed Roles**", Utilities.toString(removedRoles, ", "), false);

            Utilities.sendMessage(MMDBot.events, embed, NEUTRAL);
        }
    }

    @EventSubscriber
    public void onUserBan (UserBanEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER BANNED**");
            embed.appendField("**User**", Utilities.userString(user), false);
            embed.appendField("**Banner**", "API does not provide yet!", false);

            Utilities.sendMessage(MMDBot.events, embed, NEGATIVE);
        }
    }

    @EventSubscriber
    public void onUserPardon (UserPardonEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER PARDON**");
            embed.appendField("**User**", Utilities.userString(user), true);
            embed.appendField("**Pardoner**", "API does not provide yet!", false);

            Utilities.sendMessage(MMDBot.events, embed, POSITIVE);
        }
    }

    @EventSubscriber
    public void onUserNickNameChange (NickNameChangeEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER NICKNAME CHANGE**");
            embed.appendField("**User**", Utilities.userString(user), false);

            if (event.getOldNickname().isPresent())
                embed.appendField("**Old Nickname**", event.getOldNickname().get(), true);

            if (event.getNewNickname().isPresent())
                embed.appendField("**New Nickname**", event.getNewNickname().get(), true);

            Utilities.sendMessage(MMDBot.events, embed, NEUTRAL);
        }
    }

    @EventSubscriber
    public void onMessageDelete (MessageDeleteEvent event) {

        if (event.getGuild().equals(MMDBot.mmdGuild)) {

            final IUser user = event.getAuthor();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER DELETE MESSAGE**");
            embed.appendField("**User**", Utilities.userString(user), false);
            embed.appendField("**Chanel**", event.getChannel().getName(), false);
            embed.appendField("**Content**", Utilities.formatMessage(event.getMessage()), false);

            Utilities.sendMessage(MMDBot.events, embed, NEGATIVE);
        }
    }
}