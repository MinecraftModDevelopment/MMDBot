package com.mcmoddev.mmdbot.commander.docs;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
import org.eclipse.jdt.internal.compiler.ast.Javadoc;

import java.awt.Color;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DocsEmbed {

    public static final List<ElementTypeDisplayData> DISPLAY_DATA_LIST = List.of(
        // abstract class
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.abstractClass@2x.png",
            testType(JavadocType.Type.CLASS, elem -> elem.getModifiers().contains("abstract"))
        ),
        // exception
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.exceptionClass.svg@2x.png",
            testType(JavadocType.Type.CLASS, elem -> elem.getQualifiedName()
                .asString()
                .endsWith("Exception"))
        ),
        // fallback class
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.class@2x.png",
            testType(JavadocType.Type.CLASS)
        ),
        // enums
        new ElementTypeDisplayData(
            new Color(102, 51, 153),
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.enum.svg@2x.png",
            testType(JavadocType.Type.ENUM)
        ),
        // interfaces
        new ElementTypeDisplayData(
            Color.GREEN,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.interface.svg@2x.png",
            testType(JavadocType.Type.INTERFACE)
        ),
        // Fallback, for things like records
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.class@2x.png",
            elem -> elem instanceof JavadocType
        ),
        // methods
        new ElementTypeDisplayData(
            Color.YELLOW,
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.method.svg@2x.png",
            elem -> elem instanceof JavadocMethod
        ),
        // fields
        new ElementTypeDisplayData(
            new Color(65, 105, 225),
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.field.svg@2x.png",
            elem -> elem instanceof JavadocField
        )
    );

    private final EmbedBuilder embedBuilder;
    private final MarkdownCommentRenderer renderer;
    private final JavadocElement element;
    private final String baseUrl;
    private final DocFormatter formatter;

    public DocsEmbed(MarkdownCommentRenderer renderer, JavadocElement element, String baseUrl, final DocFormatter formatter) {
        this.renderer = renderer;
        this.element = element;
        this.baseUrl = baseUrl;
        this.formatter = formatter;

        this.embedBuilder = new EmbedBuilder();
    }

    public DocsEmbed addDeclaration() {
        embedBuilder.getDescriptionBuilder()
            .append("```java")
            .append(System.lineSeparator())
            .append(formatter.format(element, JavadocElement.DeclarationStyle.SHORT))
            .append(System.lineSeparator())
            .append("```");
        return this;
    }

    @CanIgnoreReturnValue
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

    @CanIgnoreReturnValue
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

    @CanIgnoreReturnValue
    private String renderParagraphs(JavadocComment comment, int maxLength, int maxNewlines) {
        return trimMarkdown(
            renderer.render(comment.getContent(), baseUrl),
            maxLength,
            maxNewlines
        );
    }

    @CanIgnoreReturnValue
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

    public static final String TAG_PATTERN = "(\\[.+?])\\(.+?\\)";

    @CanIgnoreReturnValue
    private boolean shouldInlineTag(String tagName, String rendered) {
        if (tagName.equals("implNote")) {
            return false;
        }

        return rendered.replaceAll(TAG_PATTERN, "$1").length() <= 100;
    }

    public DocsEmbed addIcon(LinkResolveStrategy linkResolveStrategy) {
        final var iconUrl = DISPLAY_DATA_LIST
            .stream()
            .filter(it -> it.test(element))
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

    @CanIgnoreReturnValue
    public DocsEmbed addColor() {
        DISPLAY_DATA_LIST.stream()
            .filter(it -> it.test(element))
            .findFirst()
            .map(ElementTypeDisplayData::getColor)
            .ifPresent(embedBuilder::setColor);

        return this;
    }

    @CanIgnoreReturnValue
    public DocsEmbed addFooter(String source, Duration queryDuration) {
        embedBuilder.setFooter(
            "Query resolved from index '" + source + "' in " + queryDuration.toMillis() + "ms"
        );

        return this;
    }

    @CanIgnoreReturnValue
    public MessageEmbed build() {
        return embedBuilder.build();
    }

    @CanIgnoreReturnValue
    private String limitSize(String input, int max) {
        if (input.length() <= max) {
            return input;
        }
        return input.substring(0, max - 3) + "...";
    }

    private record ElementTypeDisplayData(Color color, String iconUrl,
                                          Predicate<JavadocElement> predicate) implements Predicate<JavadocElement> {

        public Color getColor() {
            return color;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        @Override
        public boolean test(JavadocElement element) {
            return predicate.test(element);
        }
    }

    private static Predicate<JavadocElement> testType(final JavadocType.Type type) {
        return element -> element instanceof JavadocType jType && jType.getType() == type;
    }

    private static Predicate<JavadocElement> testType(final JavadocType.Type type, Predicate<JavadocType> and) {
        return element -> element instanceof JavadocType jType && jType.getType() == type && and.test(jType);
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

    @FunctionalInterface
    public interface DocFormatter {
        String format(JavadocElement element, JavadocElement.DeclarationStyle style);
    }
}
