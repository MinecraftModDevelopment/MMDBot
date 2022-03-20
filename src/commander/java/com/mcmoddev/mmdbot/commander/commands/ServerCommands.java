package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.eventlistener.DismissListener;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;

/**
 * Class holding server commands.
 */
@UtilityClass
public class ServerCommands {

    @RegisterSlashCommand
    public static final SlashCommand GUILD_COMMAND = SlashCommandBuilder.builder()
        .name("guild")
        .help("Gives info about this guild.")
        .guildOnly(true)
        .executes(event -> {
            final var guild = event.getGuild();
            final var embed = new EmbedBuilder();
            final var dateGuildCreated = Objects.requireNonNull(guild).getTimeCreated();

            embed.setTitle("Guild info");
            embed.setColor(Color.GREEN);
            embed.setThumbnail(guild.getIconUrl());
            embed.addField("Guilds name:", guild.getName(), true);
            embed.addField("Member count:", Integer.toString(guild.getMemberCount()), true);
            embed.addField("Emote count:", Long.toString(guild.getEmoteCache().size()), true);
            embed.addField("Category count:", Long.toString(guild.getCategoryCache().size()), true);
            embed.addField("Channel count:", Integer.toString(guild.getChannels().size()), true);
            embed.addField("Role count:", Long.toString(guild.getRoleCache().size()), true);
            embed.addField("Date created:", TimeFormat.DATE_TIME_LONG.format(dateGuildCreated), true);
            embed.addField("Guilds age:", TimeFormat.RELATIVE.format(dateGuildCreated), true);
            embed.setTimestamp(Instant.now());

            if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(event.getMember(),
                TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers().toArray(String[]::new))) {
                event.deferReply(false).queue(hook -> {
                    event.getGuild().retrieveCommands().queue(commands -> {
                        embed.addField("Guild registered commands", String.valueOf(commands.size()), false);
                        hook.editOriginalEmbeds(embed.build()).queue();
                    });
                });
            } else {
                event.replyEmbeds(embed.build()).queue();
            }
        })
        .build();

    @RegisterSlashCommand
    public static final SlashCommand USER_COMMAND = SlashCommandBuilder.builder()
        .name("user")
        .help("Get information about another user.")
        .options(new OptionData(OptionType.USER, "user", "The user to check."))
        .executes(event -> {
            final var embed = TheCommanderUtilities.createMemberInfoEmbed(event.getOption("user",
                event::getMember, OptionMapping::getAsMember));
            event.replyEmbeds(embed.build())
                .addActionRow(DismissListener.createDismissButton(event))
                .mentionRepliedUser(false).queue();
        })
        .build();

}