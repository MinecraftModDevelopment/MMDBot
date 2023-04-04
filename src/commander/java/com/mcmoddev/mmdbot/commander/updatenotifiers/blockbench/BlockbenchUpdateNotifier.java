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
package com.mcmoddev.mmdbot.commander.updatenotifiers.blockbench;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockbenchUpdateNotifier extends UpdateNotifier<GithubRelease> {
    public BlockbenchUpdateNotifier() {
        super(NotifierConfiguration.<GithubRelease>builder()
            .name("blockbench")
            .channelGetter(Configuration.Channels.UpdateNotifiers::blockbench)
            .versionComparator(Comparator.comparing(release -> Instant.parse(release.published_at())))
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, GithubRelease.class))
            .webhookInfo(new WebhookInfo("Blockbench Updates", "https://www.blockbench.net/favicon.png"))
            .build());
    }

    @Nullable
    @Override
    protected GithubRelease queryLatest() throws IOException {
        return BlockbenchVersionHelper.getLatest(loggingMarker);
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final GithubRelease oldVersion, final @NotNull GithubRelease newVersion) {
        return new EmbedBuilder()
            .setTitle("New Blockbench %s: %s".formatted(newVersion.prerelease() ? "pre-release" : "release", newVersion.name()), newVersion.html_url())
            .setColor(newVersion.prerelease() ? 0x29CFD8 : 0x1E93D9)
            .setDescription(Utils.truncate(Stream.of(newVersion.body().split("\n"))
                .map(str -> str.trim().startsWith("#") ? "**" + str.replace("#", "") + "**" : str)
                .collect(Collectors.joining("\n")), MessageEmbed.DESCRIPTION_MAX_LENGTH / 2)); // 4k char embed is big...
    }
}
