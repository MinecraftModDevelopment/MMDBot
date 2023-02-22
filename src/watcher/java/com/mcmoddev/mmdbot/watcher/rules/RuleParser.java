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
package com.mcmoddev.mmdbot.watcher.rules;

import com.mcmoddev.mmdbot.core.util.jda.EmbedParser;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class RuleParser {
    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor") // Why...?
    public static List<MessageCreateData> parse(String message, BiConsumer<Integer, RuleData> ruleAdder) {
        try {
            final List<MessageCreateData> messages = new ArrayList<>();
            final List<String> lines = List.of(message.split("\n"));
            final var rulesIndex = lines.indexOf(lines.stream().filter(it -> it.trim().equals("<rules>")).findFirst().orElse(""));
            int rulesEnd = lines.size() - 1;
            for (int i = rulesIndex; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("<rulesEnd/>")) {
                    rulesEnd = i;
                    break;
                }
            }
            final var embedsIndex = lines.indexOf(lines.stream().filter(it -> it.trim().equals("<embeds>")).findFirst().orElse(""));
            int embedsEnd = lines.size() - 1;
            for (int i = embedsIndex; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("<embedsEnd/>")) {
                    embedsEnd = i;
                    break;
                }
            }
            final var embeds = getEmbeds(IntStream.range(embedsIndex + 1, embedsEnd).mapToObj(lines::get).toList());
            final var messagesUntilRules = IntStream.range(0, rulesIndex).mapToObj(lines::get).toList();
            final var rules = collectRules(IntStream.range(rulesIndex + 1, rulesEnd).mapToObj(lines::get).toList());
            final var afterRules = IntStream.range(rulesEnd + 1, embedsIndex).mapToObj(lines::get).toList();

            messages.addAll(splitIntoMessages(messagesUntilRules, embeds));

            final List<MessageEmbed> currentEmbeds = new ArrayList<>();
            for (int i = 0; i < rules.size(); i++) {
                final var it = rules.get(i);
                final int showableIndex = i + 1;
                ruleAdder.accept(showableIndex, it);
                if (currentEmbeds.size() >= Message.MAX_EMBED_COUNT) {
                    messages.add(new MessageCreateBuilder().setEmbeds(currentEmbeds).build());
                    currentEmbeds.clear();
                }
                currentEmbeds.add(it.asEmbed(showableIndex).build());
            }
            if (!currentEmbeds.isEmpty()) messages.add(new MessageCreateBuilder().setEmbeds(currentEmbeds).build());

            messages.addAll(splitIntoMessages(afterRules, embeds));

            return messages;
        } catch (Exception s) {
            TheWatcher.LOGGER.error("An exception was caught while rule data!", s);
            return List.of();
        }
    }

    private static List<MessageCreateData> splitIntoMessages(final List<String> lines, final List<MessageEmbed> embedRegistry) {
        final List<MessageCreateData> messages = new ArrayList<>();
        if (lines.isEmpty()) return messages;
        MessageCreateBuilder current;
        {
            final var firstLine = lines.get(0);
            if (firstLine.startsWith("img=https://")) {
                messages.add(new MessageCreateBuilder().setContent(firstLine.substring("img=".length())).build());
                current = new MessageCreateBuilder();
            } else if (firstLine.startsWith("embed=")) {
                current = new MessageCreateBuilder().addEmbeds(embedRegistry.get(Integer.parseInt(firstLine.substring("embed=".length()))));
            } else {
                current = new MessageCreateBuilder().addContent(firstLine);
            }
        }
        for (int i = 1; i < lines.size(); i++) {
            final var it = lines.get(i);
            if (it.startsWith("img=https://")) {
                if (!current.isEmpty()) messages.add(current.build());
                messages.add(new MessageCreateBuilder().setContent(it.substring("img=".length())).build());
                current = new MessageCreateBuilder();
                continue;
            } else if (it.startsWith("embed=")) {
                if (current.getEmbeds().size() >= Message.MAX_EMBED_COUNT) {
                    messages.add(current.build());
                    current = new MessageCreateBuilder();
                }
                current.addEmbeds(embedRegistry.get(Integer.parseInt(it.substring("embed=".length()))));
                continue;
            }

            if (!current.getEmbeds().isEmpty()) {
                // Send the existing embeds as a separate message
                messages.add(current.build());
                current = new MessageCreateBuilder();
            }

            if (it.trim().startsWith("-----")) {
                messages.add(current.build());
                current = new MessageCreateBuilder();
                continue;
            }

            if ((current + "\n" + it).length() > Message.MAX_CONTENT_LENGTH) {
                messages.add(current.build());
                current = new MessageCreateBuilder();
            }
            if (!current.isEmpty())
                current.addContent("\n");
            if (!it.isBlank())
                current.addContent(it);
        }
        if (!current.isEmpty()) messages.add(current.build());
        return messages;
    }

    private static List<MessageEmbed> getEmbeds(final List<String> lines) {
        final List<MessageEmbed> embeds = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (final String line : lines) {
            if (line.startsWith("-----")) {
                embeds.add(EmbedParser.parse(current.toString()).build());
                current = new StringBuilder();
                continue;
            }
            if (!current.isEmpty())
                current.append('\n');
            if (!line.isBlank())
                current.append(line);
        }
        if (!current.isEmpty()) embeds.add(EmbedParser.parse(current.toString()).build());
        return embeds;
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

}
