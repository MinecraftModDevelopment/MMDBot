package com.mcmoddev.mmdbot.commander.docs;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import de.ialistannen.javadocapi.model.QualifiedName;
import de.ialistannen.javadocapi.querying.FuzzyQueryResult;
import de.ialistannen.javadocapi.querying.QueryApi;
import de.ialistannen.javadocapi.storage.ElementLoader;
import de.ialistannen.javadocapi.util.BaseUrlElementLoader;
import de.ialistannen.javadocapi.util.NameShortener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DocsCommand extends SlashCommand {

    public static final NameShortener NAME_SHORTENER = new NameShortener();

    final QueryApi<FuzzyQueryResult> queryApi;
    final ElementLoader loader;
    final DocsSender docsSender;
    final ComponentListener componentListener;

    public DocsCommand(final QueryApi<FuzzyQueryResult> queryApi, final ElementLoader loader, final DocsSender docsSender, final ComponentListener.Builder componentListener) {
        this.queryApi = queryApi;
        this.loader = loader;
        this.docsSender = docsSender;
        this.componentListener = componentListener
            .onButtonInteraction(this::onButtonInteraction)
            .build();

        name = "docs";
        help = "Fetches Javadoc for methods, classes and fields.";
        options = List.of(
            new OptionData(
                OptionType.STRING,
                "query",
                "The query. Example: 'String#contains'",
                true,
                true
            ),
            new OptionData(
                OptionType.BOOLEAN,
                "long",
                "Display a long version of the javadoc",
                false
            ),
            new OptionData(
                OptionType.BOOLEAN,
                "omit-tags",
                "If true the Javadoc tags will be omitted",
                false
            )
        );
    }

    protected void onButtonInteraction(final ButtonInteractionContext context) {
        DocsButtonType.valueOf(context.getItemComponentArguments().get(0)).handleClick(context, this);
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (loader instanceof ConfigBasedElementLoader cl && !cl.indexedAll()) {
            event.deferReply(true)
                .setContent("JavaDocs are still being indexed. Please wait...")
                .queue();
            return;
        }

        final var query = event.getOption("query", "", OptionMapping::getAsString);
        if (query.length() <= 2) {
            event.deferReply(true).setContent("Expected at least 2 characters for the query!");
            return;
        }

        final var shortDescription = !event.getOption("long", false, OptionMapping::getAsBoolean);
        final var omitTags = event.getOption("omit-tags", false, OptionMapping::getAsBoolean);

        final var buttonId = UUID.randomUUID();
        event.deferReply().queue(hook -> query(
            query, event.getUser().getIdLong(), buttonId, m -> hook.editOriginal(m)
                .map(msg -> {
                    final var data = new DocsButtonData(event.getUser().getIdLong(), query, shortDescription, omitTags);
                    final var component = new Component(componentListener.getName(), buttonId, data.toArguments(), Component.Lifespan.TEMPORARY);
                    componentListener.insertComponent(component);
                    return msg;
                }),
            shortDescription, omitTags,
            () -> hook.editOriginal("Could not find any result for query: '" + query + "'").queue()
        ));
    }

    void query(String query, long userId, UUID buttonId, Function<Message, RestAction<Message>> replier, boolean shortDescription, boolean omitTags, Runnable whenNoResult) {
        final var start = Instant.now();

        List<FuzzyQueryResult> results = queryApi.query(loader, query.strip())
            .stream()
            .distinct()
            .toList();

        final var duration = Duration.between(start, Instant.now());

        if (results.size() == 1) {
            return;
        }

        if (results.stream().filter(FuzzyQueryResult::isExact).count() == 1) {
            FuzzyQueryResult result = results.stream()
                .filter(FuzzyQueryResult::isExact)
                .findFirst()
                .orElseThrow();

            replyResult(userId, buttonId, replier, result, shortDescription, omitTags, duration);
            return;
        }
        if (results.stream().filter(FuzzyQueryResult::isCaseSensitiveExact).count() == 1) {
            FuzzyQueryResult result = results.stream()
                .filter(FuzzyQueryResult::isCaseSensitiveExact)
                .findFirst()
                .orElseThrow();

            replyResult(userId, buttonId, replier, result, shortDescription, omitTags, duration);
            return;
        }

        if (results.isEmpty()) {
            whenNoResult.run();
            return;
        }
        // TODO multiple results?
    }

    void replyResult(long userId, UUID buttonId, Function<Message, RestAction<Message>> replier, FuzzyQueryResult result, boolean shortDescription, boolean omitTags, Duration queryDuration) {
        final var elements = loader.findByQualifiedName(result.getQualifiedName());

        if (elements.size() == 1) {
            final var loadResult = elements.iterator().next();
            docsSender.replyWithResult(
                replier,
                loadResult,
                shortDescription,
                omitTags,
                queryDuration,
                ((BaseUrlElementLoader) loadResult.getLoader()).getLinkResolveStrategy(),
                userId,
                buttonId
            );
        } else {
            replier.apply(new MessageBuilder("Multiple results have been found for this qualified name.").build()).queue();
        }
    }

    @Override
    public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("query")) {
            final String query = event.getFocusedOption().getValue().strip();
            if (query.length() <= 2) {
                event.replyChoices(List.of()).queue();
                return;
            }

            final var results = queryApi.autocomplete(loader, query)
                .stream()
                .distinct()
                .limit(25)
                .map(DocsCommand::toQualifiedName)
                .collect(Collectors.toSet());

            final var shortened = NAME_SHORTENER.shortenMatches(results);

            event.replyChoices(
                results.stream()
                    .map(it -> new Command.Choice(
                        maybeShorten(shortened.get(it.asString())),
                        maybeShorten(it.asString())
                    ))
                    .toList())
                .setCheck(() -> !event.getInteraction().isAcknowledged())
                .queue();
        }
    }

    /**
     * Shortens the input if its length is greater than 100.
     * @param input the string to shorten
     * @return the maybe shortened string
     */
    private static String maybeShorten(String input) {
        return input.length() > 100 ? input.substring(0, 100) : input;
    }

    public static QualifiedName toQualifiedName(String asString) {
        if (asString.contains("/")) {
            String[] parts = asString.split("/");
            return new QualifiedName(parts[1], parts[0]);
        }
        return new QualifiedName(asString);
    }
}
