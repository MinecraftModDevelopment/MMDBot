package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Class containing different commands.
 */
@UtilityClass
public class Commands {

    @RegisterSlashCommand
    public static final SlashCommand CAT_FACTS = SlashCommandBuilder.builder()
        .name("catfacts")
        .help("Get a random fact about cats, you learn something new every day!")
        .guildOnly(false)
        .executes(event -> {
            final var embed = new EmbedBuilder();
            final var fact = TheCommanderUtilities.getCatFact();
            if (!"".equals(fact)) {
                embed.setColor(Constants.RANDOM.nextInt(0x1000000));
                embed.appendDescription(fact);
                embed.setFooter("Purrwered by https://catfact.ninja");

                event.replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
            }
        })
        .build();
}
