package com.mcmoddev.mmdbot.commander.docs;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.StringReader;
import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.model.comment.JavadocComment;
import de.ialistannen.javadocapi.model.comment.JavadocCommentTag;
import de.ialistannen.javadocapi.model.types.JavadocField;
import de.ialistannen.javadocapi.model.types.JavadocMethod;
import de.ialistannen.javadocapi.model.types.JavadocType;
import de.ialistannen.javadocapi.rendering.LinkResolveStrategy;
import de.ialistannen.javadocapi.rendering.MarkdownCommentRenderer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DocsEmbed {

    private final EmbedBuilder embedBuilder;
    private final MarkdownCommentRenderer renderer;
    private final JavadocElement element;
    private final String baseUrl;
    private final JavaDocFormatter declarationFormatter;

    public DocsEmbed(MarkdownCommentRenderer renderer, JavadocElement element, String baseUrl) {
        this.renderer = renderer;
        this.element = element;
        this.baseUrl = baseUrl;

        this.embedBuilder = new EmbedBuilder();
        this.declarationFormatter = new JavaDocFormatter(56);
    }

    public DocsEmbed addDeclaration() {
        String declaration = element.getDeclaration(JavadocElement.DeclarationStyle.SHORT);

        try {
            declaration = declarationFormatter.formatDeclaration(element);
        } catch (UnsupportedOperationException e) {
            TheCommander.LOGGER.error("Exception trying to format element declaration: ", e);
        }

        embedBuilder.getDescriptionBuilder()
            .append("```java\n")
            .append(declaration)
            .append("\n```\n");
        return this;
    }

    public DocsEmbed addShortDescription() {
        element.getComment()
            .ifPresent(comment -> embedBuilder.getDescriptionBuilder()
                .append(limitSize(
                    renderParagraphs(comment, 800, 8),
                    MessageEmbed.DESCRIPTION_MAX_LENGTH - embedBuilder.getDescriptionBuilder().length()
                ))
            );

        return this;
    }

    public DocsEmbed addLongDescription() {
        element.getComment()
            .ifPresent(comment -> embedBuilder.getDescriptionBuilder()
                .append(limitSize(
                    renderParagraphs(comment, Integer.MAX_VALUE, Integer.MAX_VALUE),
                    MessageEmbed.DESCRIPTION_MAX_LENGTH - embedBuilder.getDescriptionBuilder().length()
                ))
            );

        return this;
    }

    private String renderParagraphs(JavadocComment comment, int maxLength, int maxNewlines) {
        return trimMarkdown(
            renderer.render(comment.getContent(), baseUrl),
            maxLength,
            maxNewlines
        );
    }

    public DocsEmbed addTags() {
        element.getComment().ifPresent(comment -> {
            Map<String, List<JavadocCommentTag>> tags = comment.getTags()
                .stream()
                .collect(groupingBy(
                    tag -> tag.getTagName() + tag.getArgument().map(it -> " " + it).orElse(""),
                    toList()
                ));

            for (var entry : tags.entrySet()) {
                String title = entry.getKey();

                StringJoiner bodyJoiner = new StringJoiner(", ");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    JavadocCommentTag tag = entry.getValue().get(i);
                    String rendered = renderer.render(tag.getContent(), baseUrl);
                    if (bodyJoiner.length() + rendered.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                        if (i < entry.getValue().size() - 1) {
                            bodyJoiner.add("... and more");
                        }
                        break;
                    }
                    bodyJoiner.add(rendered);
                }

                String body = limitSize(bodyJoiner.toString(), MessageEmbed.VALUE_MAX_LENGTH);
                embedBuilder.addField(
                    title,
                    body,
                    shouldInlineTag(entry.getValue().get(0).getTagName(), body)
                );
            }
        });

        return this;
    }

    private boolean shouldInlineTag(String tagName, String rendered) {
        if (tagName.equals("implNote")) {
            return false;
        }

        return rendered.replaceAll("(\\[.+?])\\(.+?\\)", "$1").length() <= 100;
    }

    public DocsEmbed addIcon(LinkResolveStrategy linkResolveStrategy) {
        String iconUrl = displayDatas()
            .stream()
            .filter(it -> it.matches(element))
            .findFirst()
            .map(ElementTypeDisplayData::getIconUrl)
            .orElse("");

        embedBuilder.setAuthor(
            StringUtils.abbreviateMiddle(
                element.getQualifiedName().asStringWithModule(),
                "...",
                MessageEmbed.AUTHOR_MAX_LENGTH
            ),
            linkResolveStrategy.resolveLink(element.getQualifiedName(), baseUrl),
            iconUrl
        );

        return this;
    }

    public DocsEmbed addColor() {
        displayDatas().stream()
            .filter(it -> it.matches(element))
            .findFirst()
            .map(ElementTypeDisplayData::getColor)
            .ifPresent(embedBuilder::setColor);

        return this;
    }

    public DocsEmbed addFooter(String source, Duration queryDuration) {
        embedBuilder.setFooter(
            "Query resolved from index '" + source + "' in " + queryDuration.toMillis() + "ms"
        );

        return this;
    }

    public MessageEmbed build() {
        return embedBuilder.build();
    }

    private String limitSize(String input, int max) {
        if (input.length() <= max) {
            return input;
        }
        return input.substring(0, max - 3) + "...";
    }

    private List<ElementTypeDisplayData> displayDatas() {
        return List.of(
            new ElementTypeDisplayData(
                new Color(255, 99, 71), // tomato
                "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.abstractClass@2x.png",
                isType(it -> it.getType() == JavadocType.Type.CLASS && it.getModifiers().contains("abstract"))
            ),
            new ElementTypeDisplayData(
                new Color(255, 99, 71), // tomato
                "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.exceptionClass.svg@2x.png",
                isType(it -> it.getType() == JavadocType.Type.CLASS && it.getQualifiedName()
                    .asString()
                    .endsWith("Exception"))
            ),
            new ElementTypeDisplayData(
                new Color(255, 99, 71), // tomato
                "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.class@2x.png",
                isType(it -> it.getType() == JavadocType.Type.CLASS)
            ),
            new ElementTypeDisplayData(
                new Color(102, 51, 153), // rebecca purple
                "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.enum.svg@2x.png",
                isType(it -> it.getType() == JavadocType.Type.ENUM)
            ),
            new ElementTypeDisplayData(
                Color.GREEN,
                "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.interface.svg@2x.png",
                isType(it -> it.getType() == JavadocType.Type.INTERFACE)
            ),
            // Fallback
            new ElementTypeDisplayData(
                new Color(255, 99, 71), // tomato
                "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.class@2x.png",
                isType(it -> true)
            ),
            new ElementTypeDisplayData(
                Color.YELLOW,
                "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.method.svg@2x.png",
                isMethod(it -> true)
            ),
            new ElementTypeDisplayData(
                new Color(65, 105, 225), // royal blue,
                "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.field.svg@2x.png",
                isField(it -> true)
            )
        );
    }

    private static class ElementTypeDisplayData {

        private final Color color;
        private final String iconUrl;
        private final Predicate<JavadocElement> predicate;

        private ElementTypeDisplayData(Color color, String iconUrl,
                                       Predicate<JavadocElement> predicate) {
            this.color = color;
            this.iconUrl = iconUrl;
            this.predicate = predicate;
        }

        public Color getColor() {
            return color;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public boolean matches(JavadocElement element) {
            return predicate.test(element);
        }
    }

    private static Predicate<JavadocElement> isType(Predicate<JavadocType> inner) {
        return element -> element instanceof JavadocType && inner.test((JavadocType) element);
    }

    private static Predicate<JavadocElement> isMethod(Predicate<JavadocMethod> inner) {
        return element -> element instanceof JavadocMethod && inner.test((JavadocMethod) element);
    }

    private static Predicate<JavadocElement> isField(Predicate<JavadocField> inner) {
        return element -> element instanceof JavadocField && inner.test((JavadocField) element);
    }

    /**
     * Trims the input markdown to approximately a given length. Might be longer as it tries to finish
     * links and code blocks.
     *
     * @param input the input string
     * @param maxLength the maximum input length to try and fulfill
     * @param maxNewlines the maximum amount of newlines to try and fulfill
     * @return the trimmed markdown
     */
    public static String trimMarkdown(String input, int maxLength, int maxNewlines) {
        StringBuilder result = new StringBuilder();

        StringReader inputReader = new StringReader(input);
        int encounteredNewlines = 0;
        boolean inCodeBlock = false;

        while (inputReader.canRead()) {
            if (!inCodeBlock && (encounteredNewlines >= maxNewlines || result.length() >= maxLength)) {
                break;
            }

            char next = inputReader.readChar();
            result.append(next);

            if (next == '`' && inputReader.canRead(2) && inputReader.peek(2).equals("``")) {
                inCodeBlock = !inCodeBlock;
                result.append(inputReader.readChars(2));
                continue;
            }

            if (next == '[') {
                result.append(inputReader.readRegex(Pattern.compile(".+?]\\(.+?\\)")));
            }

            if (next == '\n') {
                encounteredNewlines++;
            }
        }

        String text = result.toString();
        result = new StringBuilder(text.strip());

        if (inputReader.canRead()) {
            int skippedLines = (int) inputReader.readRemaining().chars().filter(c -> c == '\n').count();
            result.append("\n\n*Skipped ");
            if (skippedLines > 0) {
                result.append("**");
                result.append(skippedLines);
                result.append("** line");
                if (skippedLines > 1) {
                    result.append("s");
                }
            } else {
                result.append("**the rest of the line**");
            }
            result.append(". Click `Expand` if you are intrigued.*");
        }

        return result.toString();
    }

}
