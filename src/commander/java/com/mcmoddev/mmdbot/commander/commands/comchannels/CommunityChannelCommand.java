/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.commander.commands.comchannels;

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.config.GuildConfiguration;
import com.mcmoddev.mmdbot.commander.util.dao.ComChannelsDAO;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.*;

/**
 * Create a community channel owned by the specified user.
 * Takes a user parameter and a string parameter.
 * <p>
 * Takes the form:
 * /community-channel KiriCattus Proxy's Trinkets
 * /community-channel SomebodyElse Another Channel With A Very Long Name That Discord Will Reject
 * /community-channel [user] [name]
 *
 * @author Unknown
 * @author Curle
 * @author matyrobbrt
 */
@SuppressWarnings("unused")
public final class CommunityChannelCommand extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS
        = EnumSet.of(Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);

    @RegisterSlashCommand
    public static final CommunityChannelCommand CMD = new CommunityChannelCommand();

    /**
     * Instantiates a new Cmd community channel.
     */
    public CommunityChannelCommand() {
        super();
        name = "community-channel";
        children = new SlashCommand[]{
            new Create(), new Transfer(), new Owner()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        // NO-OP
    }

    public static final class Owner extends SlashCommand {
        public Owner() {
            name = "owner";
            help = "Checks the owner of a community channel.";
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            final var owner = TheCommander.getInstance().getJdbi()
                .withExtension(ComChannelsDAO.class, db -> db.getOwner(event.getChannel().getIdLong()));
            if (owner != null) {
                event.deferReply(true).setContent("This channel is owned by <@" + owner + ">.")
                    .allowedMentions(List.of())
                    .mentionRepliedUser(false)
                    .addActionRow(DismissListener.createDismissButton(event))
                    .queue();
            } else
                event.deferReply(true).setContent("This channel is not a community channel or its owner is not known.").queue();
        }
    }

    public static final class Transfer extends SlashCommand {

        public Transfer() {
            name = "transfer";
            help = "Transfers a community channel to another member.";
            options = List.of(
                new OptionData(OptionType.USER, "member", "The member to transfer the channel to.", true)
            );
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            final var newOwner = event.getOption("member", OptionMapping::getAsMember);
            if (newOwner == null) {
                event.deferReply(true).setContent("Unknown member!").queue();
                return;
            }
            final var oldOwner = TheCommander.getInstance().getJdbi()
                .withExtension(ComChannelsDAO.class, db -> db.getOwner(event.getChannel().getIdLong()));
            if (oldOwner == null) {
                event.deferReply(true).setContent("This channel is not a community channel.").queue();
                return;
            }
            if (oldOwner == event.getIdLong() || event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class,
                    db -> db.changeOwnership(event.getChannel().getIdLong(), newOwner.getIdLong()));
                event.getTextChannel().getManager()
                    .removePermissionOverride(oldOwner)
                    .putMemberPermissionOverride(newOwner.getIdLong(), TheCommander.getInstance().getConfigForGuild(event.getGuild())
                        .channels().community().ownerPermissions(), List.of())
                    .queue();
                event.deferReply().setContent("Ownership of this channel has been transferred to " + newOwner.getAsMention())
                    .allowedMentions(EnumSet.of(Message.MentionType.USER))
                    .queue();
            } else {
                event.deferReply(true).setContent("You do not own this channel!").queue();
            }
        }
    }

    public static final class Create extends SlashCommand {

        public Create() {
            name = "create";
            help = "Creates a new community channel for the given user.";
            category = new Category("Moderation");
            arguments = "<user ID/mention> <channel name>";
            userPermissions = REQUIRED_PERMISSIONS.toArray(Permission[]::new);
            aliases = new String[]{"community-channel", "comm-ch"};
            guildOnly = true;
            botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);


            OptionData user = new OptionData(OptionType.USER, "user", "The user to create the channel for.").setRequired(true);
            OptionData channelName = new OptionData(OptionType.STRING, "channel", "The name of the channel to create.").setRequired(true);
            List<OptionData> dataList = new ArrayList<>();
            dataList.add(user);
            dataList.add(channelName);
            this.options = dataList;
        }

        /**
         * Execute.
         *
         * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
         */
        @Override
        protected void execute(final SlashCommandEvent event) {
            final var guild = Objects.requireNonNull(event.getGuild());

            final var user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsMember));
            final var channel = event.getOption("channel", "", OptionMapping::getAsString);

            final var guildCfg = Objects.requireNonNull(event.getGuildSettings(GuildConfiguration.class))
                .channels().community();

            final var category = guildCfg.category().resolve(guild::getCategoryById);
            if (category == null) {
                event.reply("Community channel category is incorrectly configured. Please contact the bot maintainers.").queue();
                return;
            }

            final var ownerPermissions = guildCfg.ownerPermissions();
            if (ownerPermissions.isEmpty()) {
                TheCommander.LOGGER.warn("Community channel owner permissions is incorrectly configured");
                event.reply("Channel owner permissions is incorrectly configured. Please contact the bot maintainers.").queue();
                return;
            }

            final Set<Permission> diff = Sets.difference(ownerPermissions, guild.getSelfMember().getPermissions());
            if (!diff.isEmpty()) {
                TheCommander.LOGGER.warn("Cannot assign permissions to channel owner due to insufficient permissions: {}", diff);
                event.reply("Cannot assign certain permissions to channel owner due to insufficient permissions; "
                    + "continuing anyway...").queue();
                ownerPermissions.removeIf(diff::contains);
            }

            event.deferReply()
                .flatMap(hook -> Objects.requireNonNull(category).createTextChannel(channel)
                    .flatMap(ch -> category.modifyTextChannelPositions()
                        .sortOrder(Comparator.comparing(GuildChannel::getName))
                        .map($ -> ch))
                    .flatMap(ch -> ch.upsertPermissionOverride(user).setAllowed(ownerPermissions).map($ -> ch))
                    .flatMap(ch -> ch.sendMessage(new MessageBuilder()
                            .append(formatMessage(
                                guildCfg.getChannelCreatedMessage(),
                                user,
                                ch
                            ))
                            .build())
                        .flatMap(Message::pin)
                        .map($ -> ch)
                    )
                    .flatMap(ch -> {
                        TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class, db -> db.insert(ch.getIdLong(), user.getIdLong()));
                        return hook.editOriginal("Successfully created community channel at " + ch.getAsMention() + "!");
                    }))
                .queue();
        }

        public static String formatMessage(String msg, Member owner, TextChannel channel) {
            return msg.replace("{user}", owner.getAsMention())
                .replace("{guild}", channel.getGuild().getName())
                .replace("{channel}", channel.getAsMention());
        }
    }

}
