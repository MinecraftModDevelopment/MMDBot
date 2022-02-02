/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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
package com.mcmoddev.mmdbot.modules.commands.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.modules.logging.LoggingModule;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.database.dao.Warnings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class CmdWarning extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    /**
     * Instantiates a new Cmd mute.
     */
    public CmdWarning() {
        super();
        name = "warning";
        help = "Does stuff regarding warnings.";
        category = new Category("Moderation");
        requiredRole = "Staff";
        guildOnly = true;

        children = new SlashCommand[]{
            new AddWarn(), new ListWarns(), new ClearWarn()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {

    }

    public static final class AddWarn extends SlashCommand {

        public AddWarn() {
            this.name = "add";
            help = "Adds a new warning to the user";
            options = List.of(new OptionData(OptionType.USER, "user", "The user to warn").setRequired(true),
                new OptionData(OptionType.STRING, "reason", "The reason of the warning").setRequired(true),
                new OptionData(OptionType.BOOLEAN, "public", "If the punishment is public"));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String reason = event.getOption("reason").getAsString();
            boolean publicPunishment = event.getOption("public") == null || event.getOption("public").getAsBoolean();
            User userToWarn = event.getOption("user").getAsUser();
            Member member = event.getMember();

            if (!canInteract(event, event.getGuild().getMember(userToWarn))) {
                return;
            }

            final UUID warnId = withExtension(doc -> doc.insert(userToWarn.getIdLong(), event.getGuild().getIdLong(), reason, member.getIdLong(), Instant.now()));

            userToWarn.openPrivateChannel().queue(channel -> {
                final var dmEmbed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("New Warning")
                    .setDescription("You have been warned in **" + event.getGuild().getName() + "**!")
                    .addField("Warner:", mentionAndID(member.getIdLong()), false)
                    .addField("Reason:", reason, false)
                    .setFooter("Warner ID: " + member.getId(), member.getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());
                channel.sendMessageEmbeds(dmEmbed.build()).queue();
            });

            final var embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("New Warning")
                .setDescription("%s warned %s".formatted(mentionAndID(member.getIdLong()), mentionAndID(userToWarn.getIdLong())))
                .setThumbnail(userToWarn.getEffectiveAvatarUrl())
                .addField("Reason:", reason, false)
                .addField("Warning ID", warnId.toString(), false)
                .setTimestamp(Instant.now())
                .setFooter("Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
            if (publicPunishment) {
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
            }
            LoggingModule.executeInLoggingChannel(LoggingModule.LoggingType.IMPORTANT, loggingChannel -> loggingChannel.sendMessageEmbeds(embed.build()).queue());
            event.getInteraction().reply(new MessageBuilder().append("Warn successful!").build()).setEphemeral(true)
                .queue();
        }
    }

    public static final class ListWarns extends SlashCommand {

        public ListWarns() {
            this.name = "list";
            help = "Lists the warnings of a user.";
            options = List
                .of(new OptionData(OptionType.USER, "user", "The user to remove the warn from").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            User userToSee = event.getOption("user").getAsUser();
            final long userID = userToSee.getIdLong();

            final var warnings = MMDBot.database().withExtension(Warnings.class, db -> db.getWarnings(userID, event.getGuild().getIdLong()));

            final EmbedBuilder embed = new EmbedBuilder()
                .setDescription("The warnings of " + mentionAndID(userID) + ":")
                .setTimestamp(Instant.now()).setColor(Color.MAGENTA);
            for (final String id : warnings) {
                embed.addField("Warning " + id + ":",
                    "Reason: **" + withExtension(db -> db.getReason(id)) + "**; Warner: " + mentionAndID(withExtension(db -> db.getModerator(id))) + "; Timestamp: "
                        + new Timestamp(TimeFormat.DATE_TIME_LONG, withExtension(db -> db.getTimestamp(id).toEpochMilli())) {
                    }.toString(),
                    false);
            }

            event.getInteraction().replyEmbeds(embed.build()).queue();
        }
    }

    public static final class ClearWarn extends SlashCommand {

        public ClearWarn() {
            this.name = "clear";
            help = "Clears a warning from the user";
            options = List.of(
                new OptionData(OptionType.USER, "user", "The user to remove the warn from").setRequired(true),
                new OptionData(OptionType.STRING, "id",
                    "The ID of the warn to remove. Do not provide it if you want to clean all warnings of that user."),
                new OptionData(OptionType.BOOLEAN, "public", "If the warning clearing is public"));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            boolean publicPunishment = event.getOption("public") == null || event.getOption("public").getAsBoolean();
            User userToWarn = event.getOption("user").getAsUser();
            String warnId = Utils.getOrEmpty(event, "id");
            Member member = event.getMember();

            if (!canInteract(event, event.getGuild().getMember(userToWarn))) {
                return;
            }

            if (warnId.isBlank()) {
                MMDBot.database().useExtension(Warnings.class, db -> db.clearAll(userToWarn.getIdLong(), event.getGuild().getIdLong()));

                userToWarn.openPrivateChannel().queue(channel -> {
                    final var dmEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Warnings Cleared")
                        .setDescription("All of your warnings from **" + event.getGuild().getName() + "** have been cleared!")
                        .setTimestamp(Instant.now())
                        .setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());;
                    channel.sendMessageEmbeds(dmEmbed.build()).queue();
                });

                final var embed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Warnings Cleared")
                    .setDescription("All of the warnings of " + userToWarn.getAsMention() + " (" + userToWarn.getId() + ") have been cleared!")
                    .setTimestamp(Instant.now())
                    .setFooter("Moderator ID: " + member.getId(), member.getEffectiveAvatarUrl());

                if (publicPunishment) {
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                }
                LoggingModule.executeInLoggingChannel(LoggingModule.LoggingType.IMPORTANT, c -> c.sendMessageEmbeds(embed.build()).queue());

                event.getInteraction().reply(new MessageBuilder().append("Warnings cleared!").build()).setEphemeral(true).queue();
            } else {
                final var warnExists = withExtension(db -> db.warningExists(warnId));
                if (!warnExists) {
                    event.deferReply(true).setContent("A warning with the specified ID could not be found").queue();
                    return;
                }

                final var warnDoc = withExtension(db -> db.getWarningDocument(warnId));

                MMDBot.database().useExtension(Warnings.class, db -> db.deleteById(warnId));

                Utils.executeInDMs(warnDoc.userId(), channel -> {
                    final var dmEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Warning Cleared")
                        .setDescription("One of your warnings from **" + event.getGuild().getName() + "** has been cleared!")
                        .addField("Old warning reason:", warnDoc.reason(), false)
                        .addField("Old warner:", mentionAndID(warnDoc.moderatorId()), false)
                        .setTimestamp(Instant.now())
                        .setFooter("Moderator ID: " + member.getId(), member.getEffectiveAvatarUrl());
                    channel.sendMessageEmbeds(dmEmbed.build()).queue();
                });

                final var embed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Warning Cleared")
                    .setDescription("One of the warnings of " + mentionAndID(warnDoc.userId()) + " has been removed!")
                    .addField("Old warning reason:", warnDoc.reason(), false)
                    .addField("Old warner:", mentionAndID(warnDoc.userId()), false)
                    .setTimestamp(Instant.now())
                    .setFooter("Moderator ID: " + member.getId(), member.getEffectiveAvatarUrl());

                if (publicPunishment) {
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                }
                LoggingModule.executeInLoggingChannel(LoggingModule.LoggingType.IMPORTANT, c -> c.sendMessageEmbeds(embed.build()).queue());

                event.getInteraction().reply(new MessageBuilder().append("Warning cleared!").build()).setEphemeral(true).queue();
            }

        }
    }

    public static boolean canInteract(final SlashCommandEvent event, final Member target) {
        if (target == null) {
            event.deferReply(true).setContent("Unknown user!").queue();
            return false;
        }

        if (target.getIdLong() == event.getMember().getIdLong()) {
            event.deferReply(true).setContent("You cannot interact with yourself!").mentionRepliedUser(false).queue();
            return false;
        }

        if (!event.getMember().canInteract(target)) {
            event.deferReply(true).setContent("You do not have permission to warn this user!").mentionRepliedUser(false)
                .queue();
            return false;
        }

        return true;
    }

    public static <R> R withExtension(Function<Warnings, R> callback) {
        return MMDBot.database().withExtension(Warnings.class, callback::apply);
    }

    private static String mentionAndID(final long id) {
        return "<@" + id + "> (" + id + ")";
    }
}
