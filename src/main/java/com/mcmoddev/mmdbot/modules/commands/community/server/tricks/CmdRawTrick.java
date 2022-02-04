package com.mcmoddev.mmdbot.modules.commands.community.server.tricks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.TrickContext;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;

public final class CmdRawTrick extends SlashCommand {

    public CmdRawTrick() {
        super();
        name = "raw";
        help = "Gets the raw representation of the trick";
        category = new Category("Management");
        arguments = "<trick_name>";
        guildOnly = true;
        options = Collections.singletonList(new OptionData(OptionType.STRING, "trick", "The trick to get.")
            .setRequired(true).setAutoComplete(true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var trickName = Utils.getOrEmpty(event, "trick");

        Tricks.getTrick(trickName).ifPresentOrElse(trick -> {
            event.replyEmbeds(new EmbedBuilder().setTitle("Raw contents of " + trickName)
                .setDescription(MarkdownUtil.codeblock(trick.getRaw())).setColor(Color.GREEN)
                .addField("Trick Names", String.join(" ", trick.getNames()), false)
                .setTimestamp(Instant.now()).setFooter("Requested by: " + event.getUser().getAsTag(),
                    event.getUser().getAvatarUrl()).build()).addActionRow(DismissListener.createDismissButton(event)).queue();
        }, () -> event.reply("This trick does not exist anymore!").setEphemeral(true).queue());
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        event.replyChoices(CmdRunTrick.getNamesStartingWith(currentChoice, 5)).queue();
    }
}
