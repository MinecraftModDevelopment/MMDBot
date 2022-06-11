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
package com.mcmoddev.mmdbot.commander.eventlistener;

import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilePreviewListener implements EventListener {

    public static final List<String> WHITELISTED_EXTENSIONS = List.of(
        "txt", "log", "java", "json", "groovy", "js",
        "cpp", "c", "gradle"
    );

    public static final Emoji DISMISS_EMOJI = Emoji.fromMarkdown("\uD83D\uDEAE️");

    public static final String URL = "https://discordbot.matyrobbrt.com/fpreview?url=";

    @Override
    public void onEvent(@NotNull final GenericEvent gE) {
        if (!(gE instanceof MessageReceivedEvent event)) return;
        final var attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty()) return;
        final var messageBuilder = new MessageBuilder()
            .append("Paste version of ");
        final var rows = new ArrayList<List<Button>>();
        addButton(rows, DismissListener.createDismissButton(event.getAuthor().getIdLong(), ButtonStyle.SECONDARY, DISMISS_EMOJI));
        for (var i = 0; i < attachments.size(); i++) {
            final var attach = attachments.get(i);
            if (attach.getFileExtension() == null)
                continue;
            if (WHITELISTED_EXTENSIONS.contains(attach.getFileExtension().toLowerCase(Locale.ROOT))) {
                final var url = URL + attach.getUrl();
                messageBuilder
                    .append('`')
                    .append(attach.getFileName())
                    .append('`');

                if (i != attachments.size() - 1) {
                    messageBuilder.append(", ");
                } else {
                    messageBuilder.append(' ');
                }
                addButton(rows, Button.link(url, trimIfTooLong("View " + attach.getFileName())));
            }
        }
        if (rows.get(0).size() > 1) { // We're guaranteed to have at least one list, with at least one button: the dismiss one
            messageBuilder.append("from ")
                .append(event.getAuthor().getAsMention());
            event.getMessage().reply(messageBuilder.build())
                .setActionRows(rows.stream().map(ActionRow::of).toArray(ActionRow[]::new))
                .mentionRepliedUser(false)
                .allowedMentions(List.of())
                .queue();
        }
    }

    private static String trimIfTooLong(String str) {
        if (str.length() > Button.LABEL_MAX_LENGTH - 3) {
            return str.substring(0, Button.LABEL_MAX_LENGTH - 3) + "...";
        } else {
            return str;
        }
    }

    private static void addButton(List<List<Button>> list, Button button) {
        if (list.isEmpty()) {
            final var nL = new ArrayList<Button>();
            nL.add(button);
            list.add(nL);
        } else {
            final var lastList = list.get(list.size() - 1);
            if (lastList.size() >= Component.Type.BUTTON.getMaxPerRow()) {
                final var newList = new ArrayList<Button>();
                newList.add(button);
                if (list.size() < 5) {
                    list.add(newList);
                }
            } else {
                lastList.add(button);
            }
        }
    }
}
