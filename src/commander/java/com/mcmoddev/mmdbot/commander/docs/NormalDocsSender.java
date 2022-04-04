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
    private final DocsEmbed.DocFormatter formatter = JavadocElement::getDeclaration;
    private final NameShortener nameShortener = new NameShortener();

    @Override
    public void replyWithResult(final Function<Message, RestAction<Message>> replier, final ElementLoader.LoadResult<JavadocElement> loadResult, final boolean shortDescription, final boolean omitTags, final Duration queryDuration, final LinkResolveStrategy linkResolveStrategy, final long userId, final UUID buttonId) {
        final var embed = new DocsEmbed(
            new MarkdownCommentRenderer(linkResolveStrategy),
            loadResult.getResult(),
            ((BaseUrlElementLoader) loadResult.getLoader()).getBaseUrl(),
            formatter
        )
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
        buttons.add(DismissListener.createDismissButton(userId));

        replier.apply(new MessageBuilder(embed.build())
            .setActionRows(ActionRow.of(buttons))
            .build())
            .queue();
    }

    public static Button button(final String id, final DocsButtonType type, ButtonStyle style) {
        return Button.of(style, Component.createIdWithArguments(id, type.toString()), type.name);
    }

    @Override
    public void replyMultipleResults(final Function<Message, RestAction<Message>> replier, final boolean shortDescription, final boolean omitTags, final Collection<FuzzyQueryResult> results, final long userId, final UUID buttonId) {
        final var nameResultMap = results.stream().collect(Collectors.toMap(
            it -> it.getQualifiedName().asString(),
            it -> it,
            (a, b) -> a
        ));
        final var shortenedNameMap = nameShortener.shortenMatches(
            nameResultMap.keySet().stream().map(QualifiedName::new).collect(Collectors.toSet())
        );
        final var labelResultList = shortenedNameMap.entrySet().stream()
            .map(it -> Map.entry(it.getValue(), nameResultMap.get(it.getKey())))
            .sorted(
                Comparator.<Map.Entry<String, FuzzyQueryResult>, Boolean>comparing(it -> it.getValue().isExact())
                    .reversed()
                    .thenComparing(Map.Entry::getKey)
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

    private List<ActionRow> buildRowsButton(List<Map.Entry<String, FuzzyQueryResult>> results, UUID id, long user) {
        final var buttonId = id.toString();
        final var counter = new AtomicInteger();

        return results.stream()
            .limit(net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON.getMaxPerRow() * 5L)
            .collect(partition(net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON.getMaxPerRow()))
            .values()
            .stream()
            .map(items -> {
                List<Button> buttons = new ArrayList<>();

                for (Entry<String, FuzzyQueryResult> entry : items) {
                    boolean exact = entry.getValue().isExact();
                    String label = entry.getKey();
                    String command = createIdWithArguments(buttonId, DocsButtonType.MULTIPLE_RESULTS.toString(), counter.getAndIncrement());
                    buttons.add(Button.of(
                        exact ? ButtonStyle.PRIMARY : ButtonStyle.SECONDARY,
                        command,
                        StringUtils.abbreviate(label, 80),
                        getEmoji(entry.getValue())
                    ));
                }

                return ActionRow.of(buttons);
            })
            .toList();
    }

    private List<ActionRow> buildRowsMenu(List<Map.Entry<String, FuzzyQueryResult>> results,
                                          UUID id, long user) {
        final var counter = new AtomicInteger();

        final var grouped = results.stream()
            .collect(groupingBy(
                it -> it.getValue().getType(),
                mapping(
                    it -> {
                        QualifiedName name = it.getValue().getQualifiedName();
                        String command = String.valueOf(counter.incrementAndGet());

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
                            .of(
                                label,
                                command
                            )
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

}
