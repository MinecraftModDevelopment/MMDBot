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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CooldownScope;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.commands.menu.message.GistContextMenu;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.gist.GistUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public final class GistCommand extends Command {

    public GistCommand() {
        name = "gist";
        help = "Creates a gist from the referenced message.";
        cooldown = 120;
        cooldownScope = CooldownScope.USER_GUILD;
        guildOnly = false;
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (TheCommander.getInstance().getGithubToken().isBlank()) {
            event.getMessage().reply("I cannot create a gist! I have not been configured to do so.").mentionRepliedUser(false).queue();
        }
        GistContextMenu.THREAD_POOL.execute(() -> {
            if (event.getMessage().isFromGuild() && TheCommanderUtilities.memberHasRoles(event.getMember(), TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers())) {
                // Remove the cooldown from bot maintainers, for testing purposes
                event.getClient().applyCooldown(getCooldownKey(event), 1);
            }
            run(TheCommander.getInstance().getGithubToken(), event);
        });
    }

    private static void run(final String token, final CommandEvent event) {
        final var target = event.getMessage().getReferencedMessage();
        if (target == null || target.isWebhookMessage() || target.getAuthor().isSystem()) {
            event.getMessage().reply("Please reference a message to create a gist from its attachments.").mentionRepliedUser(false).queue();
            return;
        }
        if (!GistContextMenu.canGist(target)) {
            event.getMessage().reply("The message doesn't have any attachments!").mentionRepliedUser(false).queue();
            return;
        }
        try {
            final var gist = GistUtils.create(token, GistContextMenu.createGistFromMessage(target));
            if (gist == null) {
                event.getMessage().reply("The Gist I created was null for some reason. Try again later.").mentionRepliedUser(false).queue();
                return;
            }
            final EmbedBuilder embed = new EmbedBuilder().setColor(Color.MAGENTA).setTimestamp(Instant.now())
                .setFooter("Requester ID: " + event.getMember().getIdLong(), event.getMember().getEffectiveAvatarUrl())
                .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                .setDescription("A gist has been created for the attachments of [this](%s) message.".formatted(target.getJumpUrl()))
                .addField("Gist Link", gist.htmlUrl(), false);
            event.getMessage().replyEmbeds(embed.build()).mentionRepliedUser(false).queue();
        } catch (InterruptedException | ExecutionException | GistUtils.GistException e) {
            event.getMessage().replyFormat("Error while creating gist: **%s**", e.getLocalizedMessage()).mentionRepliedUser(false).queue();
            TheCommander.LOGGER.error("Error while creating gist", e);
        }
    }

}
