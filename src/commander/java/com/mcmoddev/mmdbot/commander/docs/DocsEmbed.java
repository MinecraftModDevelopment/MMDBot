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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mcmoddev.mmdbot.core.util.StringReader;
import com.mcmoddev.mmdbot.core.util.StringUtilities;
import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.model.comment.JavadocComment;
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
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DocsEmbed extends EmbedBuilder {

    public static final List<ElementTypeDisplayData> DISPLAY_DATA_LIST = List.of(
        // abstract class
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/Groovy.icons.groovy.abstractClass@2x.png",
            testClass(elem -> elem.getModifiers().contains("abstract"))
        ),
        // exception
        new ElementTypeDisplayData(
            new Color(255, 99, 71),
            "https://www.jetbrains.com/help/img/idea/2019.1/icons.nodes.exceptionClass.svg@2x.png",
            testClass(elem -> elem.getQualifiedName()
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

    private final MarkdownCommentRenderer renderer;
    private final JavadocElement element;
    private final String baseUrl;
    private final DocFormatter formatter;

    public DocsEmbed(MarkdownCommentRenderer renderer, JavadocElement element, String baseUrl, final DocFormatter formatter) {
        this.renderer = renderer;
        this.element = element;
        this.baseUrl = baseUrl;
        this.formatter = formatter;
    }

    public DocsEmbed addDeclaration() {
        getDescriptionBuilder()
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
            .ifPresent(comment -> getDescriptionBuilder()
                .append(limitSize(
                    renderParagraphs(comment, 800, 8),
                    MessageEmbed.DESCRIPTION_MAX_LENGTH - getDescriptionBuilder().length()
                ))
            );

        return this;
    }

    @CanIgnoreReturnValue
    public DocsEmbed addLongDescription() {
        element.getComment()
            .ifPresent(comment -> getDescriptionBuilder()
                .append(limitSize(
                    renderParagraphs(comment, Integer.MAX_VALUE, Integer.MAX_VALUE),
                    MessageEmbed.DESCRIPTION_MAX_LENGTH - getDescriptionBuilder().length()
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
        if (element.getComment().isEmpty()) return this;
        element.getComment().get().getTags()
            .stream()
            .collect(groupingBy(
                tag -> tag.getTagName() + tag.getArgument().map(it -> " " + it).orElse(""),
                toList()
            ))
            .forEach((title, tags) -> {
                final var bodyJoiner = new StringJoiner(", ");
                if (title.equals("author") && tags.size() > 1) {
                    title = "authors";
                }
                for (int i = 0; i < tags.size(); i++) {
                    final var tag = tags.get(i);
                    final var rendered = renderer.render(tag.getContent(), baseUrl);
                    if (bodyJoiner.length() + rendered.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                        if (i < tags.size() - 1) {
                            bodyJoiner.add("... and more");
                        }
                        break;
                    }
                    bodyJoiner.add(rendered);
                }

                final var body = limitSize(bodyJoiner.toString(), MessageEmbed.VALUE_MAX_LENGTH);
                addField(
                    StringUtilities.uppercaseFirstLetter(title),
                    body,
                    shouldInlineTag(tags.get(0).getTagName(), body)
                );
            });

        return this;
    }

    public static final String TAG_PATTERN = "(\\[.+?])\\(.+?\\)";

    private boolean shouldInlineTag(String tagName, String rendered) {
        if (tagName.equals("implNote")) {
            return false;
        }

        return rendered.replaceAll(TAG_PATTERN, "$1").length() <= 100;
    }

    @CanIgnoreReturnValue
    public DocsEmbed addIcon(LinkResolveStrategy linkResolveStrategy) {
        final var iconUrl = DISPLAY_DATA_LIST
            .stream()
            .filter(it -> it.test(element))
            .findFirst()
            .map(ElementTypeDisplayData::iconUrl)
            .orElse(null);

        setAuthor(
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
            .map(ElementTypeDisplayData::colour)
            .ifPresent(this::setColor);

        return this;
    }

    @CanIgnoreReturnValue
    public DocsEmbed addFooter(String source, Duration queryDuration) {
        setFooter("Query resolved from source '%s' in %sms".formatted(source, queryDuration.toMillis()));
        return this;
    }

    @CanIgnoreReturnValue
    private String limitSize(String input, int max) {
        if (input.length() <= max) {
            return input;
        }
        return input.substring(0, max - 3) + "...";
    }

    private record ElementTypeDisplayData(Color colour, String iconUrl,
                                          Predicate<JavadocElement> predicate) implements Predicate<JavadocElement> {
        @Override
        public boolean test(JavadocElement element) {
            return predicate.test(element);
        }
    }

    private static Predicate<JavadocElement> testType(final JavadocType.Type type) {
        return element -> element instanceof JavadocType jType && jType.getType() == type;
    }

    private static Predicate<JavadocElement> testClass(Predicate<JavadocType> and) {
        return element -> element instanceof JavadocType jType && jType.getType() == JavadocType.Type.CLASS && and.test(jType);
    }

    private static final Pattern CLOSING_BRACKET = Pattern.compile(".+?]\\(.+?\\)");

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
        var builder = new StringBuilder();

        final var reader = new StringReader(input);
        var encounteredNewlines = 0;
        var inCodeBlock = false;

        while (reader.canRead()) {
            if (!inCodeBlock && (encounteredNewlines >= maxNewlines || builder.length() >= maxLength)) {
                break;
            }

            char next = reader.readChar();
            builder.append(next);

            if (next == '`' && reader.canRead(2) && reader.peek(2).equals("``")) {
                inCodeBlock = !inCodeBlock;
                builder.append(reader.readChars(2));
                continue;
            }

            if (next == '[') {
                builder.append(reader.readRegex(CLOSING_BRACKET));
            }

            if (next == '\n') {
                encounteredNewlines++;
            }
        }

        var text = builder.toString();
        builder = new StringBuilder(text.strip());

        if (reader.canRead()) {
            if (inCodeBlock) {
                // finish code block
                builder.append("`");
            }
            int skippedLines = (int) reader.readRemaining().chars().filter(c -> c == '\n').count();
            builder.append("\n\n*Skipped ");
            if (skippedLines > 0) {
                builder.append("**");
                builder.append(skippedLines);
                builder.append("** line");
                if (skippedLines > 1) {
                    builder.append("s");
                }
            } else {
                builder.append("**the rest of the line**");
            }
            builder.append(". Click `Expand` to show more.*");
        }

        return builder.toString();
    }

    @FunctionalInterface
    public interface DocFormatter {
        String format(JavadocElement element, JavadocElement.DeclarationStyle style);
    }
}
