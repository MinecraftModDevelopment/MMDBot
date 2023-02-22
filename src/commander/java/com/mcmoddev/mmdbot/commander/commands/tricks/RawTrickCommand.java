/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.commands.tricks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.ScriptTrick;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.Color;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;

public final class RawTrickCommand extends SlashCommand {

    public RawTrickCommand() {
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
        if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }

        final var trickName = event.getOption("trick", "", OptionMapping::getAsString);

        Tricks.getTrick(trickName).ifPresentOrElse(trick -> {
            event.replyEmbeds(new EmbedBuilder().setTitle("Raw contents of " + trickName)
                .setDescription(MarkdownUtil.codeblock(trick instanceof ScriptTrick ? "js" : null, trick.getRaw())).setColor(Color.GREEN)
                .addField("Trick Names", String.join(" ", trick.getNames()), false)
                .setTimestamp(Instant.now()).setFooter("Requested by: " + event.getUser().getAsTag(),
                    event.getUser().getAvatarUrl()).build()).addActionRow(DismissListener.createDismissButton(event)).queue();
        }, () -> event.reply("This trick does not exist anymore!").setEphemeral(true).queue());
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        event.replyChoices(RunTrickCommand.getNamesStartingWith(currentChoice, 5)).queue();
    }
}
