package com.mcmoddev.bot.handlers;

import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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

    @EventSubscriber
    public void onReady (ReadyEvent event) {

        Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), "MMDBot is up and ready!");
    }

    @EventSubscriber
    public void onUserJoin (UserJoinEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER JOIN**");
            embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), true);
            embed.appendField("**User Account Creation**", String.valueOf(user.getCreationDate()), true);
            embed.appendField("**Server Member Count**", String.valueOf(event.getGuild().getTotalMemberCount()), false);
            embed.ignoreNullEmptyFields();
            embed.withColor((int) (Math.random() * 0x1000000));

            Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
        }
    }

    @EventSubscriber
    public void onUserRoles (UserRoleUpdateEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();

            List<IRole> newRoles = event.getNewRoles();
            List<IRole> oldRoles = event.getOldRoles();

            embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), false);
            embed.appendField("**Current Roles**", arrayToString(newRoles, 0, ", "), true);

            embed.ignoreNullEmptyFields();
            embed.withColor((int) (Math.random() * 0x1000000));

            if (newRoles.size() < oldRoles.size()) {
                embed.withDescription("**USER ROLES REMOVE**");

                List<IRole> diff = new ArrayList<>(oldRoles);
                diff.removeAll(newRoles);

                embed.appendField("**Roles Removed**", arrayToString(diff, 0, ", "), true);

                Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
            }
            else if (newRoles.size() > oldRoles.size()) {
                embed.withDescription("**USER ROLES ADD**");

                List<IRole> diff = new ArrayList<>(newRoles);
                diff.removeAll(oldRoles);

                embed.appendField("**Roles Added**", arrayToString(diff, 0, ", "), true);

                Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
            }

        }
    }

    public static String arrayToString (List<IRole> array, int start, String delimiter) {

        String ret = "";
        for (int i = start; i < array.size(); i++) {
            ret += array.get(i).getName();
            if (i < array.size() - 1)
                ret += delimiter;
        }
        return ret;
    }

    @EventSubscriber
    public void onUserLeave (UserLeaveEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER LEAVE**");
            embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), true);
            embed.appendField("**Server Member Count**", String.valueOf(event.getGuild().getTotalMemberCount()), false);
            embed.withColor((int) (Math.random() * 0x1000000));

            Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
        }
    }

    @EventSubscriber
    public void onUserBan (UserBanEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
        IUser user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        embed.withDescription("**USER BANNED**");
        embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), true);
        embed.withColor((int) (Math.random() * 0x1000000));

        Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
    }
    }

    @EventSubscriber
    public void onUserPardon (UserPardonEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER PARDON**");
            embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), true);
            embed.withColor((int) (Math.random() * 0x1000000));

            Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
        }
    }

    @EventSubscriber
    public void onUserNickNameChange (NickNameChangeEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getUser();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER NICKNAME CHANGE**");
            embed.appendField("**User**", user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), false);
            if (event.getOldNickname().isPresent()) {
                embed.appendField("**Old Nickname**", event.getOldNickname().get(), true);
            }
            if (event.getNewNickname().isPresent()) {
                embed.appendField("**New Nickname**", event.getNewNickname().get(), true);
            }

            embed.withColor((int) (Math.random() * 0x1000000));

            Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
        }
    }

    @EventSubscriber
    public void onMessageDelete (MessageDeleteEvent event) {

        if (event.getGuild().getID().equals(MMDBot.MMD_GUILD_ID)) {
            IUser user = event.getAuthor();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.withDescription("**USER DELETE MESSAGE**");
            embed.appendField(Utilities.makeBold("User"), user.getName() + "#" + user.getDiscriminator() + System.lineSeparator() + user.getID(), false);

            embed.appendField("Content", event.getMessage().getContent(), false);
            embed.withColor((int) (Math.random() * 0x1000000));

            Utilities.sendMessage(MMDBot.INSTANCE.getChannelByID(MMDBot.EVENTS_CHANNEL_ID), embed.build());
        }
    }
}
