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
package com.mcmoddev.mmdbot.commander.commands.menu.message;

import com.jagrosh.jdautilities.command.CooldownScope;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.Constants;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.gist.Gist;
import com.mcmoddev.mmdbot.core.util.gist.GistUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GistContextMenu extends MessageContextMenu {

    public static final Executor THREAD_POOL = Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "GistCreator"), true));

    public GistContextMenu() {
        name = "Gist";
        cooldown = 10;
        cooldownScope = CooldownScope.USER;
    }

    @Override
    protected void execute(final MessageContextMenuEvent event) {
        if (TheCommander.getInstance().getGithubToken().isBlank()) {
            event.deferReply(true).setContent("I cannot create a gist! I have not been configured to do so.");
        }
        THREAD_POOL.execute(() -> {
            if (event.isFromGuild() && TheCommanderUtilities.memberHasRoles(event.getMember(), TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
                // Remove the cooldown from bot maintainers, for testing purposes
                event.getClient().applyCooldown(getCooldownKey(event), 1);
            }
            run(TheCommander.getInstance().getGithubToken(), event);
        });
    }

    private static void run(final String token, final MessageContextMenuEvent event) {
        if (!canGist(event.getTarget())) {
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
                TheCommander.LOGGER.error("Error while creating gist", e);
            }
        });
    }

    public static String generateName(int length) {
        // Create a random name for the file, to prevent any conflicts
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        return Constants.RANDOM.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(length).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public static final List<String> BLACKLISTED_ATTACHMENTS = List.of("png", "jpg", "jpeg", "webm", "mp4", "hevc");

    public static Gist createGistFromMessage(final net.dv8tion.jda.api.entities.Message message)
        throws InterruptedException, ExecutionException {
        final var hasCodeBlocks = message.getContentRaw().contains("```");
        final var gist = hasCodeBlocks ? new Gist("", false) : new Gist(message.getContentRaw(), false);
        if (hasCodeBlocks) {
            var content = message.getContentRaw().substring(message.getContentRaw().indexOf("```") + 3);
            final var indexOf$ = content.indexOf("\n");
            final var extension = content.substring(0, indexOf$ == -1 ? 0 : indexOf$);
            content = content.substring(content.indexOf("\n") + 1);
            content = content.substring(0, content.lastIndexOf("```"));
            gist.addFile("file" + (extension.isBlank() ? "" : "." + extension), content);
        }
        for (var attach : message.getAttachments()) {
            attach.retrieveInputStream().thenAccept(is -> {
                if (BLACKLISTED_ATTACHMENTS.contains(attach.getFileExtension())) {
                    return;
                }
                final String fileName = generateName(10) + "." + attach.getFileExtension();
                try {
                    gist.addFile(fileName, GistUtils.readInputStream(is));
                } catch (IOException e) {
                    TheCommander.LOGGER.error("Error while reading file for creating a gist", e);
                }
            }).get();
        }
        return gist;
    }

    public static boolean canGist(Message message) {
        return message.getAttachments().isEmpty() || !message.getContentRaw().contains("```");
    }
}
