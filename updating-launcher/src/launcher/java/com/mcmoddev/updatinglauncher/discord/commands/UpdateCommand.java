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
package com.mcmoddev.updatinglauncher.discord.commands;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.updatinglauncher.Config;
import com.mcmoddev.updatinglauncher.DefaultJarUpdater;
import com.mcmoddev.updatinglauncher.api.JarUpdater;
import com.mcmoddev.updatinglauncher.api.Release;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateCommand extends ULCommand {
    public UpdateCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(jarUpdater, config);
        name = "update";
        help = "Updates the process' jar.";
        options = List.of(
            new OptionData(OptionType.STRING, "tag", "The tag to which to update. Don't provide to update to latest.")
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var jarUpdater = this.jarUpdater.get();
        final var tagOption = event.getOption("tag");
        if (tagOption == null) {
            event.reply("Trying to update to latest...")
                .map(hook -> {
                    final Release latest;
                    try {
                        latest = jarUpdater.getUpdateChecker().resolveLatestRelease();
                    } catch (IOException | InterruptedException e) {
                        return hook.editOriginal("Could not find latest release.");
                    }
                    return Optional.ofNullable(latest)
                        .map(rel -> hook.editOriginal("Found release \"%s\". Updating...".formatted(latest.name()))
                        .flatMap(msg -> {
                            try {
                                jarUpdater.killAndUpdate(latest);
                                return msg.editMessage("Successfully updated to release \"%s\"!".formatted(latest.name()));
                            } catch (Exception e) {
                                return msg.editMessage("Exception while trying to update the old jar: " + e.getLocalizedMessage());
                            }
                        })).orElseGet(() -> hook.editOriginal("Cannot update to latest release as I can't find a matching asset in it."));
                })
                .queue(RestAction::queue);
        } else {
            final var tag = tagOption.getAsString();
            event.deferReply()
                .setContent("Trying to update to tag: " + tag)
                .map(hook -> {
                    try {
                        final var release = jarUpdater.getUpdateChecker().getReleaseByTagName(tag);
                        return Optional.ofNullable(release)
                            .map(asset -> hook.editOriginal("Found release \"%s\". Updating...".formatted(release.name()))
                            .flatMap(msg -> {
                                try {
                                    jarUpdater.killAndUpdate(release);
                                    return msg.editMessage("Successfully updated to release \"%s\"!".formatted(release.name()));
                                } catch (Exception e) {
                                    return msg.editMessage("Exception while trying to update the old jar: " + e.getLocalizedMessage());
                                }
                            })).orElseGet(() -> hook.editOriginal("Cannot update to the specified tag as I can't find a matching asset in it, or it doesn't exist."));
                    } catch (Exception e) {
                        return hook.editOriginal("Exception trying to find tag: " + e.getLocalizedMessage());
                    }
                })
                .queue(RestAction::queue);
        }
    }

}
