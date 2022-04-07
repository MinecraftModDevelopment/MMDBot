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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatedCommand;
import com.mcmoddev.mmdbot.core.commands.paginate.Paginator;
import com.mcmoddev.mmdbot.core.commands.paginate.PaginatorBuilder;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Slf4j
public final class XkcdCommand extends PaginatedCommand {

    public static final Paginator.ButtonFactory BUTTON_FACTORY = Paginator.DEFAULT_BUTTON_FACTORY.with(Paginator.ButtonType.DISMISS, id -> Button.secondary(id, Emoji.fromUnicode("\uD83D\uDEAE")));
    public static final URL LATEST_COMIC = Utils.rethrowSupplier(() -> new URL("https://xkcd.com/info.0.json")).get();
    public static final Random RANDOM = new Random();

    @RegisterSlashCommand
    public static final XkcdCommand COMMAND = new XkcdCommand(Paginator.builder(TheCommander.getComponentListener("xkcd-cmd"))
        .itemsPerPage(1)
        .dismissible(true)
        .buttonsOwnerOnly(true)
        .buttonOrder(Paginator.ButtonType.FIRST, Paginator.ButtonType.PREVIOUS, Paginator.ButtonType.DISMISS, Paginator.ButtonType.NEXT, Paginator.ButtonType.LAST)
        .buttonFactory(BUTTON_FACTORY)
    );

    private XkcdCommand(final PaginatorBuilder paginator) {
        super(paginator);
        name = "xkcd";
        help = "Resolves a xkcd comic.";
        options = List.of(
            new OptionData(OptionType.INTEGER, "number", "The number of the comic to get. Defaults to random. Use -1 for the latest comic.")
        );
        guildOnly = false;
    }

    @Override
    protected EmbedBuilder getEmbed(final int startingIndex, final int maximum, final List<String> arguments) {
        try {
            final var xkcd = getComic(startingIndex + 1);
            return new EmbedBuilder()
                .setTitle(xkcd.safe_title() + " #" + xkcd.num(), "https://xkcd.com/" + xkcd.num())
                .setDescription(xkcd.alt())
                .setImage(xkcd.img())
                .addField("Date", xkcd.day() + "/" + xkcd.month() + "/" + xkcd.year(), true);
        } catch (IOException e) {
            log.warn("Exception trying to resolve comic ", e);
            return new EmbedBuilder()
                .setDescription("There was an exception trying to retrieve that comic: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply()
            .queue(hook -> {
                try {
                    final var latest = getLatestComic().num();
                    var id = event.getOption("number", RANDOM.nextInt(latest - 1) + 1, OptionMapping::getAsInt);
                    if (id == -1) {
                        id = latest;
                    }
                    hook.editOriginal(paginator.createPaginatedMessage(id - 1, latest, event.getUser().getIdLong())).queue();
                } catch (IOException e) {
                    hook.editOriginal("There was an exception executing that command: " + e.getLocalizedMessage())
                        .setActionRow(DismissListener.createDismissButton())
                        .queue();
                    log.error("Exception executing command ", e);
                }
            });
    }

    public Xkcd getLatestComic() throws IOException {
        try (final var reader = new InputStreamReader(LATEST_COMIC.openStream())) {
            return Constants.Gsons.NO_PRETTY_PRINTING.fromJson(reader, Xkcd.class);
        }
    }

    public Xkcd getComic(int number) throws IOException {
        try (final var reader = new InputStreamReader(new URL("https://xkcd.com/%s/info.0.json".formatted(number)).openStream())) {
            return Constants.Gsons.NO_PRETTY_PRINTING.fromJson(reader, Xkcd.class);
        }
    }

    public record Xkcd(String month, int num, String link, String year, String news, String safe_title, String transcript, String alt, String img, String title, String day) { }

}
