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
package com.mcmoddev.mmdbot.watcher.commands.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.event.moderation.WarningEvent;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.database.Warnings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class CmdWarning extends SlashCommand {

    public static final Permission[] REQUIRED_PERMISSIONS = new Permission[] {
        Permission.MODERATE_MEMBERS
    };

    /**
     * Instantiates a new Cmd Warning.
     */
    public CmdWarning() {
        super();
        name = "warning";
        help = "Does stuff regarding warnings.";
        category = new Category("Moderation");
        guildOnly = true;
        userPermissions = REQUIRED_PERMISSIONS;

        children = new SlashCommand[]{
            new AddWarn(), new ListWarns(), new ClearWarn()
        };
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        if (Objects.equals(event.getSubcommandName(), "clear")) {
            children[2].onAutoComplete(event);
        }
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
            userPermissions = REQUIRED_PERMISSIONS;
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
            Events.MODERATION_BUS.post(new WarningEvent.Add(event.getGuild().getIdLong(), member.getIdLong(),
                userToWarn.getIdLong(), withExtension(doc -> doc.getWarningDocument(warnId.toString()))));
            event.getInteraction().reply(new MessageBuilder().append("Warn successful!").build()).setEphemeral(true)
                .queue();
        }
    }

    public static final class ListWarns extends PaginatedCommand {

        public ListWarns() {
            super(TheWatcher.getComponentListener("list-warns-cmd"), Component.Lifespan.TEMPORARY, 10, true);
            this.name = "list";
            help = "Lists the warnings of a user.";
            options = List
                .of(new OptionData(OptionType.USER, "user", "The user whose warnings to see.").setRequired(true));
            userPermissions = REQUIRED_PERMISSIONS;
            guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            User userToSee = event.getOption("user").getAsUser();
            final long userID = userToSee.getIdLong();

            final var warnings = TheWatcher.database().withExtension(Warnings.class, db -> db.getWarningsForUser(userID, event.getGuild().getIdLong()));

            // Args: userId, guildId
            sendPaginatedMessage(event, warnings.size(), userToSee.getId(), event.getGuild().getId());
        }

        @Override
        protected EmbedBuilder getEmbed(final int startingIndex, final int maximum, final List<String> arguments) {
            final var userID = Long.parseLong(arguments.get(0));
            final var warnings = TheWatcher.database().withExtension(Warnings.class, db -> db.getWarningsForUser(userID, Long.parseLong(arguments.get(1))));

            final EmbedBuilder embed = new EmbedBuilder()
                .setDescription("The warnings of " + mentionAndID(userID) + ":")
                .setTimestamp(Instant.now()).setColor(Color.MAGENTA);
            for (var i = startingIndex; i < Math.min(startingIndex + paginator.getItemsPerPage(), maximum); i++) {
                final var id = warnings.get(i);
                embed.addField("Warning " + id + ":",
                    "Reason: **" + withExtension(db -> db.getReason(id)) + "**; Warner: " + mentionAndID(withExtension(db -> db.getModerator(id))) + "; Timestamp: "
                        + TimeFormat.DATE_TIME_LONG.format(withExtension(db -> db.getTimestamp(id))),
                    false);
            }
            return embed;
        }
    }

    public static final class ClearWarn extends SlashCommand {

        public ClearWarn() {
            this.name = "clear";
            help = "Clears a warning from the user";
            options = List.of(
                new OptionData(OptionType.USER, "user", "The user to remove the warn from").setRequired(true),
                new OptionData(OptionType.STRING, "id",
                    "The ID of the warn to remove. Do not provide it if you want to clean all warnings of that user.").setAutoComplete(true),
                new OptionData(OptionType.BOOLEAN, "public", "If the warning clearing is public"));
            userPermissions = REQUIRED_PERMISSIONS;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            final Boolean publicPunishment = event.getOption("public", true, OptionMapping::getAsBoolean);
            final User userToWarn = event.getOption("user").getAsUser();
            final String warnId = event.getOption("id", "", OptionMapping::getAsString);
            final Member member = event.getMember();

            if (!canInteract(event, event.getGuild().getMember(userToWarn))) {
                return;
            }

            if (warnId.isBlank()) {
                TheWatcher.database().useExtension(Warnings.class, db -> db.clearAll(userToWarn.getIdLong(), event.getGuild().getIdLong()));

                userToWarn.openPrivateChannel().queue(channel -> {
                    final var dmEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Warnings Cleared")
                        .setDescription("All of your warnings from **" + event.getGuild().getName() + "** have been cleared!")
                        .setTimestamp(Instant.now())
                        .setFooter("Un-Warner ID: " + member.getId(), member.getEffectiveAvatarUrl());
                    ;
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
                Events.MODERATION_BUS.post(new WarningEvent.ClearAllWarns(event.getGuild().getIdLong(), member.getIdLong(), userToWarn.getIdLong()));

                event.getInteraction().reply(new MessageBuilder().append("Warnings cleared!").build()).setEphemeral(true).queue();
            } else {
                final var warnExists = withExtension(db -> db.warningExists(warnId));
                if (!warnExists) {
                    event.deferReply(true).setContent("A warning with the specified ID could not be found").queue();
                    return;
                }

                final var warnDoc = withExtension(db -> db.getWarningDocument(warnId));

                TheWatcher.database().useExtension(Warnings.class, db -> db.deleteById(warnId));

                event.getJDA().retrieveUserById(warnDoc.userId())
                        .flatMap(User::openPrivateChannel)
                        .queue(channel -> {
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
                Events.MODERATION_BUS.post(new WarningEvent.Clear(event.getGuild().getIdLong(), member.getIdLong(), userToWarn.getIdLong(), withExtension(doc -> doc.getWarningDocument(warnId))));

                event.getInteraction().reply(new MessageBuilder().append("Warning cleared!").build()).setEphemeral(true).queue();
            }
        }

        @Override
        public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
            final var currentChoice = event.getInteraction().getFocusedOption().getValue();
            event.replyChoices(withExtension(Warnings::getAllWarnings)
                .stream()
                .filter(id -> id.startsWith(currentChoice))
                .limit(5)
                .map(id -> {
                    final var targetMember = event.getJDA().getUserById(CmdWarning.<Long>withExtension(db -> db.getUser(id)));
                    return new Command.Choice(id + (targetMember == null ? "" : (" - " + targetMember.getAsTag())), id);
                }).toList()).queue();
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
        return TheWatcher.database().withExtension(Warnings.class, callback::apply);
    }

    private static String mentionAndID(final long id) {
        return "<@" + id + "> (" + id + ")";
    }
}
