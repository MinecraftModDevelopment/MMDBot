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
package com.mcmoddev.mmdbot.commander.docs;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.model.QualifiedName;
import de.ialistannen.javadocapi.querying.FuzzyQueryResult;
import de.ialistannen.javadocapi.rendering.LinkResolveStrategy;
import de.ialistannen.javadocapi.rendering.MarkdownCommentRenderer;
import de.ialistannen.javadocapi.storage.ElementLoader;
import de.ialistannen.javadocapi.util.BaseUrlElementLoader;
import de.ialistannen.javadocapi.util.NameShortener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static com.mcmoddev.mmdbot.core.commands.component.Component.createIdWithArguments;

public class NormalDocsSender implements DocsSender {

    // TODO maybe remove the `abstract` part from interfaces, and other similar cases
    // Maybe also add the parameters an annotation can have
    private final DocsEmbed.DocFormatter formatter = JavadocElement::getDeclaration;
    private final NameShortener nameShortener = new NameShortener();

    @Override
    public void replyWithResult(final Function<Message, RestAction<Message>> replier, final ElementLoader.LoadResult<JavadocElement> loadResult, final boolean shortDescription, final boolean omitTags, final Duration queryDuration, final LinkResolveStrategy linkResolveStrategy, final long userId, final UUID buttonId) {
        final var embed = new DocsEmbed(
            new MarkdownCommentRenderer(linkResolveStrategy),
            loadResult.getResult(),
            loadResult.getLoader() instanceof BaseUrlElementLoader urlLoader ? urlLoader.getBaseUrl() : "",
            formatter
        );
        embed
            .addColor()
            .addIcon(linkResolveStrategy)
            .addDeclaration()
            .addFooter(loadResult.getLoader().toString(), queryDuration);

        if (shortDescription) {
            embed.addShortDescription();
        } else {
            embed.addLongDescription();
        }
        if (!omitTags) {
            embed.addTags();
        }

        final var btnId = buttonId.toString();
        final var buttons = new ArrayList<Button>();
        if (shortDescription) {
            buttons.add(button(btnId, DocsButtonType.EXPAND, ButtonStyle.SECONDARY));
        } else {
            buttons.add(button(btnId, DocsButtonType.COLLAPSE, ButtonStyle.SECONDARY));
        }
        if (omitTags) {
            buttons.add(button(btnId, DocsButtonType.ADD_TAGS, ButtonStyle.SECONDARY));
        } else {
            buttons.add(button(btnId, DocsButtonType.REMOVE_TAGS, ButtonStyle.SECONDARY));
        }
        buttons.add(DismissListener.createDismissButton(userId, ButtonStyle.DANGER, "\uD83D\uDDD1️ Dismiss"));

        replier.apply(new MessageBuilder(embed.build())
            .setContent(null)
            .setActionRows(ActionRow.of(buttons))
            .build())
        .queue();
    }

    public static Button button(final String id, final DocsButtonType type, ButtonStyle style) {
        return Button.of(style, Component.createIdWithArguments(id, type.toString()), type.name);
    }

    @Override
    public void replyMultipleResults(final Function<Message, RestAction<Message>> replier, final boolean shortDescription, final boolean omitTags, final List<FuzzyQueryResult> results, final long userId, final UUID buttonId) {
        final var nameResultMap = results.stream().collect(Collectors.toMap(
            it -> it.getQualifiedName().asString(),
            it -> it,
            (a, b) -> a
        ));
        final var shortenedNameMap = nameShortener.shortenMatches(
            nameResultMap.keySet().stream().map(QualifiedName::new).collect(Collectors.toSet())
        );
        final var labelResultList = shortenedNameMap.entrySet().stream()
            .map(it -> {
                final var value = nameResultMap.get(it.getKey());
                // keep the index, as it's needed for component IDs to be exact
                return new IndexedPair<>(results.indexOf(value), it.getValue(), nameResultMap.get(it.getKey()));
            })
            .sorted(
                Comparator.<IndexedPair<String, FuzzyQueryResult>, Boolean>comparing(it -> it.getValue().isExact())
                    .reversed()
                    .thenComparing(IndexedPair::getKey)
            )
            .toList();

        List<ActionRow> rows;
        if (labelResultList.size() <= 5 * net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON.getMaxPerRow()) {
            rows = buildRowsButton(labelResultList, buttonId, userId);
        } else {
            rows = buildRowsMenu(labelResultList, buttonId, userId);
        }

        Message message = new MessageBuilder("I found (at least) the following Elements:  \n")
            .setActionRows(rows)
            .build();

        replier.apply(message).queue();
    }

    private List<ActionRow> buildRowsButton(List<IndexedPair<String, FuzzyQueryResult>> results, UUID id, long user) {
        final var buttonId = id.toString();

        return results.stream()
            .limit(net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON.getMaxPerRow() * 5L)
            .collect(partition(net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON.getMaxPerRow()))
            .values()
            .stream()
            .map(items -> {
                final List<Button> buttons = new ArrayList<>();

                for (final var entry : items) {
                    final var exact = entry.getValue().isExact();
                    buttons.add(Button.of(
                        exact ? ButtonStyle.PRIMARY : ButtonStyle.SECONDARY,
                        createIdWithArguments(buttonId, DocsButtonType.MULTIPLE_RESULTS.toString(), entry.index()),
                        StringUtils.abbreviate(entry.getKey(), 80),
                        getEmoji(entry.getValue())
                    ));
                }

                return ActionRow.of(buttons);
            })
            .toList();
    }

    private List<ActionRow> buildRowsMenu(List<IndexedPair<String, FuzzyQueryResult>> results,
                                          UUID id, long user) {
        final var grouped = results.stream()
            .collect(groupingBy(
                it -> it.getValue().getType(),
                mapping(
                    it -> {
                        final var name = it.getValue().getQualifiedName();

                        String label;
                        if (name.asString().contains("#")) {
                            label = name.getLexicalParent()
                                .map(p -> p.getSimpleName() + "#")
                                .orElse("");
                            label += name.getSimpleName();
                        } else {
                            label = name.getSimpleName();
                        }

                        label = StringUtils.abbreviateMiddle(label, "...", 25);

                        return SelectOption
                            .of(label, String.valueOf(it.index()))
                            .withDescription(StringUtils.abbreviate(it.getKey(), 50))
                            .withEmoji(getEmoji(it.getValue()));
                    },
                    Collectors.toList()
                )
            ));

        return grouped.entrySet().stream()
            .limit(5)
            .map(it ->
                SelectMenu.create(id.toString())
                    .addOptions(it.getValue().stream().limit(25).collect(toList()))
                    .setPlaceholder(StringUtils.capitalize(it.getKey().name().toLowerCase(Locale.ROOT)))
                    .build()
            )
            .map(ActionRow::of)
            .toList();
    }

    private Emoji getEmoji(FuzzyQueryResult result) {
        if (result.getQualifiedName().asString().endsWith("Exception")) {
            return Emoji.fromMarkdown("<:Exception:871325719127547945>");
        }

        return switch (result.getType()) {
            case METHOD -> Emoji.fromMarkdown("<:Method:871140776711708743>");
            case FIELD -> Emoji.fromMarkdown("<:Field:871140776346791987>");
            case ANNOTATION -> Emoji.fromMarkdown("<:Annotation:871325719563751444>");
            case ENUM -> Emoji.fromMarkdown("<:Enum:871325719362412594>");
            case INTERFACE -> Emoji.fromMarkdown("<:Interface:871325719576318002>");
            case CLASS -> Emoji.fromMarkdown("<:Class:871140776900440074>");
        };
    }

    private static <T> Collector<T, ?, Map<Integer, List<T>>> partition(int groupSize) {
        final var counter = new AtomicInteger();
        return Collectors.groupingBy(
            it -> counter.getAndIncrement() / groupSize,
            Collectors.toList()
        );
    }

    record IndexedPair<K, V>(int index, K key, V value) {
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}
