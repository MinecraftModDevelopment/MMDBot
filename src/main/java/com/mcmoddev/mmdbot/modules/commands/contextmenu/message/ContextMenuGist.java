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
package com.mcmoddev.mmdbot.modules.commands.contextmenu.message;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.References;
import com.mcmoddev.mmdbot.gist.Gist;
import com.mcmoddev.mmdbot.gist.GistUtils;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ContextMenuGist extends MessageContextMenu {

    public static final Executor THREAD_POOL = Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "GistCreator"), true));

    public ContextMenuGist() {
        name = "Gist";
        cooldown = 120;
        cooldownScope = CooldownScope.USER_GUILD;
    }

    @Override
    protected void execute(final MessageContextMenuEvent event) {
        if (!GistUtils.hasToken()) {
            event.deferReply(true).setContent("I cannot create a gist! I have not been configured to do so.");
        }
        THREAD_POOL.execute(() -> run(MMDBot.getConfig().getGithubToken(), event));
    }

    private static void run(final String token, final MessageContextMenuEvent event) {
        if (event.getTarget().getAttachments().isEmpty()) {
            event.deferReply(true).setContent("The message doesn't have any attachments!").queue();
            return;
        }
        event.deferReply().queue(hook -> {
            try {
                final var gist = GistUtils.create(token, createGistFromMessage(event.getTarget()));
                if (gist == null) {
                    hook.editOriginal("The Gist I created was null for some reason. Try again later.").queue();
                    return;
                }
                final EmbedBuilder embed = new EmbedBuilder().setColor(Color.MAGENTA).setTimestamp(Instant.now())
                    .setFooter("Requester ID: " + event.getMember().getIdLong(), event.getMember().getEffectiveAvatarUrl())
                    .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription("A gist has been created for the attachments of [this](%s) message.".formatted(event.getTarget().getJumpUrl()))
                    .addField("Gist Link", gist.htmlUrl(), false);
                hook.editOriginalEmbeds(embed.build()).queue();
            } catch (InterruptedException | ExecutionException | GistUtils.GistException e) {
                hook.editOriginal(String.format("Error while creating gist: **%s**", e.getLocalizedMessage())).queue();
                MMDBot.LOGGER.error("Error while creating gist", e);
            }
        });
    }

    public static String generateName(int length) {
        // Create a random name for the file, to prevent any conflicts
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        return References.RANDOM.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public static Gist createGistFromMessage(final net.dv8tion.jda.api.entities.Message message)
        throws InterruptedException, ExecutionException {
        final var gist = new Gist(message.getContentRaw(), false);
        for (var attach : message.getAttachments()) {
            attach.retrieveInputStream().thenAccept(is -> {
                final String fileName = generateName(10) + "." + attach.getFileExtension();
                try {
                    gist.addFile(fileName, GistUtils.readInputStream(is));
                } catch (IOException e) {
                    MMDBot.LOGGER.error("Error while reading file for creating a gist", e);
                }
            }).get();
        }
        return gist;
    }
}
