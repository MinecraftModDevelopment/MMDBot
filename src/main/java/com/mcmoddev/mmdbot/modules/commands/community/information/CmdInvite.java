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
package com.mcmoddev.mmdbot.modules.commands.community.information;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.BotConfig;
import com.mcmoddev.mmdbot.modules.commands.community.PaginatedCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.database.dao.Invites;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jdbi.v3.core.extension.ExtensionCallback;
import org.jdbi.v3.core.extension.ExtensionConsumer;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CmdInvite extends SlashCommand {

    public CmdInvite() {
        name = "invite";
        guildOnly = false;
        help = "Things regarding invites";
        children = new SlashCommand[]{
            new Get(), new Add(), new Remove(), new ListCmd()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {

    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        if (Objects.equals(event.getSubcommandName(), "get")) {
            children[0].onAutoComplete(event);
        }
        if (Objects.equals(event.getSubcommandName(), "remove")) {
            children[2].onAutoComplete(event);
        }
    }

    private final class Add extends SlashCommand {

        public Add() {
            name = "add";
            help = "Adds a new invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server.").setRequired(true),
                new OptionData(OptionType.STRING, "link", "The invite link").setRequired(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            if (!event.isFromGuild()) {
                event.deferReply(true).setContent("This command can only be used in a guild!").queue();
                return;
            }

            if (!Utils.memberHasRole(event.getMember(), MMDBot.getConfig().getRole(BotConfig.RoleType.BOT_MAINTAINER))) {
                event.deferReply(true).setContent("You need to be a bot maintainer in order to use this command!").queue();
                return;
            }

            final var name = event.getOption("name").getAsString().toLowerCase(Locale.ROOT);
            var link = event.getOption("link").getAsString();

            if (!link.startsWith("https://discord.gg/") || !link.startsWith("discord.gg/")) {
                event.deferReply(true).setContent("Please provide a valid link, starting with `https://discord.gg/`").queue();
                return;
            }
            if (link.startsWith("discord.gg/")) {
                link = "https://" + link;
            }

            final String finalLink = link;
            useExtension(db -> db.insert(name, finalLink));
            event.reply("Invite added!").queue();
        }
    }

    private final class Remove extends SlashCommand {

        public Remove() {
            name = "remove";
            help = "Removes an invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server whose invite to remove.").setRequired(true)
                .setAutoComplete(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            if (!event.isFromGuild()) {
                event.deferReply(true).setContent("This command can only be used in a guild!").queue();
                return;
            }

            if (!Utils.memberHasRole(event.getMember(), MMDBot.getConfig().getRole(BotConfig.RoleType.BOT_MAINTAINER))) {
                event.deferReply(true).setContent("You need to be a bot maintainer in order to use this command!").queue();
                return;
            }

            final var name = event.getOption("name").getAsString().toLowerCase(Locale.ROOT);

            useExtension(db -> db.delete(name));
            event.reply("Invite removed!").queue();
        }

        @Override
        public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
            final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            event.replyChoices(withExtension(Invites::getAllNames).stream()
                .filter(n -> n.startsWith(currentChoice)).limit(5).map(s -> new Command.Choice(s, s)).toList()).queue();
        }
    }

    private final class Get extends SlashCommand {

        public Get() {
            name = "get";
            help = "Gets an invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server to get the invite for.")
                .setRequired(true).setAutoComplete(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            final var name = event.getOption("name").getAsString().toLowerCase(Locale.ROOT);

            withExtension(db -> db.getLink(name)).ifPresentOrElse(invite ->
                event.deferReply().setContent(invite).queue(), () -> event.deferReply(true).setContent("This invite does not exist!")
                .queue());
        }

        @Override
        public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
            final var currentChoice = event.getInteraction().getFocusedOption().getValue().toLowerCase(Locale.ROOT);
            event.replyChoices(withExtension(Invites::getAllNames).stream()
                .filter(n -> n.startsWith(currentChoice)).limit(5).map(s -> new Command.Choice(s, s)).toList()).queue();
        }
    }

    public static final class ListCmd extends PaginatedCommand {

        private static ButtonListener buttonListener;

        public ListCmd() {
            super("list", "Lists all the invites", false, List.of(), 10);
            this.listener = new ButtonListener() {
                @Override
                public String getButtonID() {
                    return "listinvites";
                }
            };
            buttonListener = this.listener;
        }

        public static ButtonListener getButtonListener() {
            return buttonListener;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            updateMaximum(withExtension(Invites::getAllNames).size());
            sendPaginatedMessage(event);
        }


        @Override
        protected EmbedBuilder getEmbed(final int from) {
            return new EmbedBuilder()
                .setTitle("Invites")
                .setDescription(withExtension(Invites::getAllNames)
                    .subList(from, Math.min(from + items_per_page, maximum))
                    .stream()
                    .reduce("", (a, b) -> a + "\n" + b))
                .setTimestamp(Instant.now());
        }
    }

    public static void useExtension(ExtensionConsumer<Invites, RuntimeException> consumer) {
        MMDBot.database().useExtension(Invites.class, consumer);
    }

    public static <T> T withExtension(ExtensionCallback<T, Invites, RuntimeException> consumer) {
        return MMDBot.database().withExtension(Invites.class, consumer);
    }

}
