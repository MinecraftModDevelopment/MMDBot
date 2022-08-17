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
package com.mcmoddev.mmdbot.watcher.rules;

import com.mcmoddev.mmdbot.core.util.jda.EmbedParser;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class RuleParser {
    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor") // Why...?
    public static List<Message> parse(String message, BiConsumer<Integer, RuleData> ruleAdder) {
        try {
            final List<Message> messages = new ArrayList<>();
            final List<String> lines = List.of(message.split("\n"));
            final var rulesIndex = lines.indexOf(lines.stream().filter(it -> it.trim().equals("## Rules")).findFirst().orElse(""));
            int rulesEnd = lines.size() - 1;
            for (int i = rulesIndex; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("<rulesEnd/>")) {
                    rulesEnd = i;
                    break;
                }
            }
            final var messagesUntilRules = IntStream.range(0, rulesIndex).mapToObj(lines::get).toList();
            final var rules = collectRules(IntStream.range(rulesIndex + 1, rulesEnd).mapToObj(lines::get).toList());
            final var afterRules = IntStream.range(rulesEnd + 1, lines.size()).mapToObj(lines::get).toList();

            messages.addAll(splitIntoMessages(messagesUntilRules));

            final List<MessageEmbed> currentEmbeds = new ArrayList<>();
            for (int i = 0; i < rules.size(); i++) {
                final var it = rules.get(i);
                final int showableIndex = i + 1;
                ruleAdder.accept(showableIndex, it);
                if (currentEmbeds.size() >= Message.MAX_EMBED_COUNT) {
                    messages.add(new MessageBuilder().setEmbeds(currentEmbeds).build());
                    currentEmbeds.clear();
                }
                currentEmbeds.add(it.asEmbed(showableIndex).build());
            }
            if (!currentEmbeds.isEmpty()) messages.add(new MessageBuilder().setEmbeds(currentEmbeds).build());

            messages.addAll(splitIntoMessages(afterRules));

            return messages;
        } catch (Exception s) {
            TheWatcher.LOGGER.error("An exception was caught while rule data!", s);
            return List.of();
        }
    }

    private static List<Message> splitIntoMessages(final List<String> lines) {
        final List<Message> messages = new ArrayList<>();
        if (lines.isEmpty()) return messages;
        AccessibleBuilder current;
        {
            final var firstLine = lines.get(0);
            if (firstLine.startsWith("img=https://")) {
                messages.add(new AccessibleBuilder().setContent(firstLine.substring("img=".length())).build());
                current = new AccessibleBuilder();
            } else if (firstLine.startsWith("embed=https://")) {
                current = new AccessibleBuilder().addEmbed(EmbedParser.parse(readText(firstLine.substring("embed=".length()))).build());
            } else {
                current = new AccessibleBuilder().append(firstLine);
            }
        }
        for (int i = 1; i < lines.size(); i++) {
            final var it = lines.get(i);
            if (it.startsWith("img=https://")) {
                if (!current.isEmpty()) messages.add(current.build());
                messages.add(new AccessibleBuilder().setContent(it.substring("img=".length())).build());
                current = new AccessibleBuilder();
                continue;
            } else if (it.startsWith("embed=https://")) {
                if (current.getEmbeds().size() >= Message.MAX_EMBED_COUNT) {
                    messages.add(current.build());
                    current = new AccessibleBuilder();
                }
                current.addEmbed(EmbedParser.parse(readText(it.substring("embed=".length()))).build());
                continue;
            }

            if (!current.getEmbeds().isEmpty()) {
                // Send the existing embeds as a separate message
                messages.add(current.build());
                current = new AccessibleBuilder();
            }

            if ((current + "\n" + it).length() > Message.MAX_CONTENT_LENGTH) {
                messages.add(current.build());
                current = new AccessibleBuilder();
            }
            if (!current.isEmpty())
                current.append('\n');
            if (!it.isBlank())
                current.append(it);
        }
        if (!current.isEmpty()) messages.add(current.build());
        return messages;
    }

    private static String readText(String url) {
        try (final var is = new URL(url).openStream()) {
            return new String(is.readAllBytes());
        } catch (Exception ignored) {
            return "";
        }
    }

    private static List<RuleData> collectRules(final List<String> lines) {
        return lines.stream().map(it -> {
            final var titleIndex = it.indexOf(':');
            // TODO support defined colours
            return new RuleData(it.substring(0, titleIndex).trim(), actual(it.substring(titleIndex + 1).trim()));
        }).toList();
    }

    private static String actual(String data) {
        return data.replace(";n", "\n");
    }

    protected static final class AccessibleBuilder extends MessageBuilder {
        public List<MessageEmbed> getEmbeds() {
            return embeds;
        }

        public AccessibleBuilder addEmbed(MessageEmbed embed) {
            embeds.add(embed);
            return this;
        }

        @NotNull
        @Override
        public AccessibleBuilder append(@Nullable final CharSequence text) {
            return (AccessibleBuilder) super.append(text);
        }
    }
}
