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
package com.mcmoddev.mmdbot.commander.commands.curseforge;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CurseForgeManager;
import com.mcmoddev.mmdbot.core.commands.paginate.Paginator;
import com.mcmoddev.mmdbot.core.util.Utils;
import io.github.matyrobbrt.curseforgeapi.request.query.ModSearchQuery;
import io.github.matyrobbrt.curseforgeapi.schemas.mod.ModAuthor;
import io.github.matyrobbrt.curseforgeapi.schemas.mod.ModLoaderType;
import io.github.matyrobbrt.curseforgeapi.util.Constants;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CFProjectCommand {
    public static final int DEFAULT_GAME_ID = Constants.GameIDs.MINECRAFT;
    public static final SubcommandGroupData GROUP = new SubcommandGroupData("project", "Commands related CurseForge projects.");

    static final class Search extends CurseForgeCommand {
        static final Paginator PAGINATOR = Paginator.builder(TheCommander.getComponentListener("cf-project-search-cmd"))
            .itemsPerPage(40)
            .message(Search::getMessage)
            .buttonsOwnerOnly(true)
            .build();
        static final ErrorHandler ERROR_HANDLER = new ErrorHandler()
            .ignore(IllegalStateException.class);

        Search() {
            name = "search";
            help = "Searches a project using the provided parameters";
            options = List.of(
                new OptionData(OptionType.INTEGER, "game", "The ID of the game to search the project in. Minecraft, if not specified.").setAutoComplete(true),
                new OptionData(OptionType.STRING, "search-filter", "Filter by free text search in the project name and author."),
                new OptionData(OptionType.INTEGER, "category", "The category to search in.").setAutoComplete(true),
                new OptionData(OptionType.STRING, "game-version", "The game version to search for."),
                new OptionData(OptionType.STRING, "slug", "Filter by slug"),
                new OptionData(OptionType.INTEGER, "mod-loader", "Filter by mod loader")
                    .addChoices(Arrays.stream(ModLoaderType.values())
                        .map(l -> new Command.Choice(l.toString(), l.ordinal() + 1))
                        .toList()),
                new OptionData(OptionType.INTEGER, "sort-field", "Filter by sort field")
                    .addChoices(Arrays.stream(ModSearchQuery.SortField.values())
                        .map(l -> new Command.Choice(Utils.uppercaseFirstLetter(l.toString().toLowerCase(Locale.ROOT)), l.ordinal() + 1))
                        .toList())
            );
            subcommandGroup = GROUP;
        }

        @Override
        protected void execute(final CFCommandContext context, final CurseForgeManager manager) throws CurseForgeException {
            final var gameId = context.getOption("game", DEFAULT_GAME_ID, OptionMapping::getAsInt);
            final var category = context.getOption("category", null, OptionMapping::getAsInt);
            final var query = ModSearchQuery.of(gameId)
                .searchFilter(context.getOption("search-filter", OptionMapping::getAsString))
                .gameVersion(context.getOption("game-version", null, OptionMapping::getAsString))
                .slug(context.getOption("slug", null, OptionMapping::getAsString))
                .modLoaderType(context.getOption("mod-loader", null, m -> ModLoaderType.byId(m.getAsInt())))
                .sortField(context.getOption("sort-field", null, m -> ModSearchQuery.SortField.values()[m.getAsInt() - 1]));
            if (category != null) {
                query.categoryId(category);
            }
            manager.api().getAsyncHelper().searchMods(query)
                .queue(response -> response.ifPresentOrElse(mods -> {
                    final var args = new ArrayList<String>();
                    args.add(context.getUser().getId());
                    mods.stream().map(m -> "%s (id: %s)".formatted(m.name(), m.id())).forEach(args::add);
                    final var msg = PAGINATOR.createPaginatedMessage(0, mods.size(), context.getUser().getIdLong(), args);
                    context.replyMessageAsAction(MessageEditData.fromCreateData(msg)).queue();
                }, () -> context.reply("API responded with status code **%s**".formatted(response.getStatusCode()))));
        }

        // Args: user, mods...
        public static @NotNull MessageCreateBuilder getMessage(int startingIndex, int maximum, final List<String> arguments) {
            final var embed = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setColor(Color.GREEN)
                .setTitle("Project Search Result");
            setEmbedAuthor(embed);
            final var user = TheCommander.getJDA().getUserById(arguments.get(0));
            if (user != null) {
                embed.setFooter("Requested by: " + user.getName(), user.getAvatarUrl());
            }
            if (arguments.size() > startingIndex + 1) {
                arguments.subList(startingIndex + 1, Math.min(maximum, arguments.size()))
                    .forEach(d -> embed.appendDescription(d).appendDescription(System.lineSeparator()));
            }
            return new MessageCreateBuilder().setEmbeds(embed.build());
        }

        @Override
        public void onAutoComplete1(final CommandAutoCompleteInteractionEvent event) throws CurseForgeException {
            if (Objects.equals(event.getFocusedOption().getName(), "game")) {
                final var cSelection = event.getFocusedOption().getValue();
                event.replyChoices(getGameChoices(cSelection))
                    .setCheck(() -> !event.isAcknowledged())
                    .queue(null, ERROR_HANDLER);
            }
            if (Objects.equals(event.getFocusedOption().getName(), "category")) {
                final var cSelection = event.getFocusedOption().getValue();
                final var gameId = event.getOption("game", DEFAULT_GAME_ID, OptionMapping::getAsInt);
                if (TheCommander.getInstance().getCurseForgeManager().isPresent()) {
                    TheCommander.getInstance().getCurseForgeManager().get().api().getAsyncHelper()
                        .getCategories(gameId)
                        .queue(r -> r.ifPresent(categories -> {
                            event.replyChoices(categories.stream()
                                    .filter(c -> c.name().startsWith(cSelection))
                                    .limit(20)
                                    .map(c -> new Command.Choice(c.name(), c.id()))
                                    .toArray(Command.Choice[]::new))
                                .setCheck(() -> !event.isAcknowledged())
                                .queue();
                        }));
                }
            }
        }
    }

    static final class Info extends CurseForgeCommand {
        Info() {
            name = "info";
            help = "Shows information about a CurseForge project.";
            options = List.of(new OptionData(OptionType.INTEGER, "id",
                "The ID of the project to get information about.", true));
            subcommandGroup = GROUP;
        }

        @Override
        protected void execute(final CFCommandContext context, final CurseForgeManager manager) throws CurseForgeException {
            final var pId = context.getOption("id", 0, OptionMapping::getAsInt);
            manager.api().getAsyncHelper().getMod(pId).queue(response -> {
                response.ifPresentOrElse(mod -> {
                    final var embed = new EmbedBuilder()
                        .setTimestamp(Instant.now())
                        .setColor(Color.GREEN)
                        .setFooter("Requested by: " + context.getUser().getName(), context.getUser().getAvatarUrl())
                        .setTitle(mod.name(), mod.links().websiteUrl())
                        .setThumbnail(mod.logo().thumbnailUrl())
                        .setDescription(mod.summary())
                        .addField("Slug", mod.slug(), true)
                        .addField("Downloads", String.valueOf((int) mod.downloadCount()), true)
                        .addField("Authors", mod.authors()
                            .stream()
                            .map(ModAuthor::name)
                            .reduce("", (a, b) -> a + System.lineSeparator() + b), true)
                        .addField("Created", TimeFormat.DATE_LONG.format(mod.getDateReleasedAsInstant()), true)
                        .addField("Latest Game Versions", mod.latestFiles().stream()
                            .flatMap(f -> f.gameVersions().stream())
                            .distinct()
                            .reduce("", (a, b) -> a + System.lineSeparator() + b), true)
                        .addField("Last Updated", TimeFormat.DATE_TIME_SHORT.format(mod.getDateModifiedAsInstant()), true)
                        .addField("Popularity Rank", String.valueOf(mod.gamePopularityRank()), true)
                        .addField("Categories", mod.categories()
                            .stream()
                            .map(io.github.matyrobbrt.curseforgeapi.schemas.Category::name)
                            .reduce("", (a, b) -> a + System.lineSeparator() + b), true)
                        .addField("Featured", String.valueOf(mod.isFeatured()), true)
                        .addField("Latest Files and their CurseMaven Coordinates", mod.latestFiles()
                            .stream()
                            .map(f -> "[%s](%s): `curse.maven:%s-%s:%s`".formatted(f.displayName(), f.downloadUrl(),
                                mod.slug().replace("-", "_") /* Replace dashes, just in case */, mod.id(), f.id()))
                            .limit(5)
                            .reduce("", (a, b) -> a + System.lineSeparator() + b), false);
                    setEmbedAuthor(embed);
                    context.replyEmbeds(embed.build());
                }, () -> context.reply("Could not find project with ID: **%s**".formatted(pId)));
            });
        }
    }
}
