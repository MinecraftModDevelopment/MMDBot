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
package com.mcmoddev.mmdbot.utilities.console;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.ThreadedEventListener;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

public class ConsoleChannelButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull final ButtonInteractionEvent event) {
        if (event.getChannel().getIdLong() != MMDBot.getConfig().getChannel("console") || event.getButton().getId() == null || !event.getButton().getId().startsWith("show_stacktrace_")) {
            return;
        }
        final var exceptionId = UUID.fromString(event.getButton().getId().replaceAll("show_stacktrace_", ""));
        if (!ThreadedEventListener.CAUGHT_EXCEPTIONS.containsKey(exceptionId)) {
            event.deferReply(true).setContent("I do not know anything about this exception anymore, but you can always check the logs. Sorry!");
            return;
        }
        final var exception = ThreadedEventListener.CAUGHT_EXCEPTIONS.get(exceptionId);
        final var newContent = new StringBuilder();
        newContent.append(event.getMessage().getContentRaw())
                .append(System.lineSeparator())
                .append("Stacktrace:")
                .append(System.lineSeparator());
        for (int i = 0; i < exception.getStackTrace().length; i++) {
            newContent.append("     at ").append(exception.getStackTrace()[i].toString())
                .append(System.lineSeparator());
        }
        event.getMessage().editMessage(newContent).setActionRows(Collections.emptyList()).queue($ -> ThreadedEventListener.CAUGHT_EXCEPTIONS.remove(exceptionId));
    }
}
