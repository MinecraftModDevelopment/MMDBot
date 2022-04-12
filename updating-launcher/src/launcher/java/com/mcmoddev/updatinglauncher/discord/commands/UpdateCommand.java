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
import com.mcmoddev.updatinglauncher.Constants;
import com.mcmoddev.updatinglauncher.JarUpdater;
import com.mcmoddev.updatinglauncher.github.Release;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Supplier;

public class UpdateCommand extends ULCommand {
    private final Supplier<JarUpdater> jarUpdater;
    public UpdateCommand(final Supplier<JarUpdater> jarUpdater, final Config.Discord config) {
        super(config);
        name = "update";
        help = "Updates the process' jar.";
        this.jarUpdater = jarUpdater;
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
                    final var as = jarUpdater.resolveAssetFromRelease(latest);
                    return as.map(asset -> hook.editOriginal("Found release \"%s\". Updating...".formatted(latest.name))
                        .flatMap(msg -> {
                            try {
                                jarUpdater.killAndUpdate(latest, asset);
                                return msg.editMessage("Successfully updated to release \"%s\"!".formatted(latest.name));
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
                        final var release = getReleaseByTagName(tag);
                        if (release == null) {
                            return hook.editOriginal("Could not find release with tag **%s**.".formatted(tag));
                        }
                        final var as = jarUpdater.resolveAssetFromRelease(release);
                        return as.map(asset -> hook.editOriginal("Found release \"%s\". Updating...".formatted(release.name))
                            .flatMap(msg -> {
                                try {
                                    jarUpdater.killAndUpdate(release, asset);
                                    return msg.editMessage("Successfully updated to release \"%s\"!".formatted(release.name));
                                } catch (Exception e) {
                                    return msg.editMessage("Exception while trying to update the old jar: " + e.getLocalizedMessage());
                                }
                            })).orElseGet(() -> hook.editOriginal("Cannot update to the specified tag as I can't find a matching asset in it."));
                    } catch (Exception e) {
                        return hook.editOriginal("Exception trying to find tag: " + e.getLocalizedMessage());
                    }
                })
                .queue(RestAction::queue);
        }
    }

    @Nullable
    public Release getReleaseByTagName(final String tag) throws Exception {
        final var jarUpdater = this.jarUpdater.get();
        final var updateChecker = jarUpdater.getUpdateChecker();
        final var uri = URI.create("https://api.github.com/repos/%s/%s/releases/tags/%s".formatted(updateChecker.getOwner(), updateChecker.getRepo(), tag));
        final var request = HttpRequest.newBuilder(uri)
            .GET()
            .header("accept", "application/vnd.github.v3+json")
            .build();

        final var res = updateChecker.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 404) {
            return null;
        }
        return Constants.GSON.fromJson(res.body(), Release.class);
    }
}
