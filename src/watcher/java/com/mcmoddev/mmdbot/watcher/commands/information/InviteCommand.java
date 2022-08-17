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
package com.mcmoddev.mmdbot.watcher.commands.information;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.database.Invites;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jdbi.v3.core.extension.ExtensionCallback;
import org.jdbi.v3.core.extension.ExtensionConsumer;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InviteCommand extends SlashCommand {

    public InviteCommand() {
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

    private static final class Add extends SlashCommand {

        public Add() {
            name = "add";
            help = "Adds a new invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server.").setRequired(true),
                new OptionData(OptionType.STRING, "link", "The invite link").setRequired(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!event.isFromGuild()) {
                event.deferReply(true).setContent("This command can only be used in a guild!").queue();
                return;
            }

            if (!TheWatcher.isBotMaintainer(event.getMember())) {
                event.deferReply(true).setContent("You need to be a bot maintainer in order to use this command!").queue();
                return;
            }

            final var name = event.getOption("name").getAsString().toLowerCase(Locale.ROOT);
            var link = event.getOption("link").getAsString();

            if (!link.startsWith("https://discord.gg/")) {
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

    private static final class Remove extends SlashCommand {

        public Remove() {
            name = "remove";
            help = "Removes an invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server whose invite to remove.").setRequired(true)
                .setAutoComplete(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!event.isFromGuild()) {
                event.deferReply(true).setContent("This command can only be used in a guild!").queue();
                return;
            }

            if (!TheWatcher.isBotMaintainer(event.getMember())) {
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

    private static final class Get extends SlashCommand {

        public Get() {
            name = "get";
            help = "Gets an invite";
            options = List.of(new OptionData(OptionType.STRING, "name", "The name of the server to get the invite for.")
                .setRequired(true).setAutoComplete(true));
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
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

        public ListCmd() {
            super(TheWatcher.getComponentListener("invites-list-cmd"), Component.Lifespan.TEMPORARY, 10);
            name = "list";
            help = "Lists all the invites";
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            sendPaginatedMessage(event, withExtension(Invites::getAllNames).size());
        }

        @Override
        protected EmbedBuilder getEmbed(final int from, final int maximum, final List<String> arguments) {
            return new EmbedBuilder()
                .setTitle("Invites")
                .setDescription(withExtension(i -> i.getAllNames().stream()
                    .map(n -> MarkdownUtil.maskedLink(Utils.uppercaseFirstLetter(n),
                        withExtension(db -> db.getLink(n)).get())).toList())
                    .subList(from, Math.min(from + paginator.getItemsPerPage(), maximum))
                    .stream()
                    .reduce("", (a, b) -> a + "\n" + b))
                .setTimestamp(Instant.now());
        }
    }

    public static void useExtension(ExtensionConsumer<Invites, RuntimeException> consumer) {
        TheWatcher.getInstance().getJdbi().useExtension(Invites.class, consumer);
    }

    public static <T> T withExtension(ExtensionCallback<T, Invites, RuntimeException> consumer) {
        return TheWatcher.getInstance().getJdbi().withExtension(Invites.class, consumer);
    }

}
