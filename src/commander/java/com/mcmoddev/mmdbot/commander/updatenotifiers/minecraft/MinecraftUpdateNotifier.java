/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2026 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.minecraft.MinecraftVersionHelper.VersionsInfo;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.Color;

public final class MinecraftUpdateNotifier extends UpdateNotifier<MinecraftVersionHelper.VersionsInfo> {

    private static final String CHANGELOG_BASE_URL = "https://www.minecraft.net/en-us/article/minecraft-";

    public MinecraftUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftVersionHelper.VersionsInfo>builder()
            .name("minecraft")
            .channelGetter(Configuration.Channels.UpdateNotifiers::minecraft)
            .versionComparator(NotifierConfiguration.notEqual())
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, VersionsInfo.class))
            .webhookInfo(new WebhookInfo("Minecraft Updates", "https://media.discordapp.net/attachments/957353544493719632/1005934698767323156/unknown.png?width=594&height=594"))
            .build());
    }

    @Override
    protected VersionsInfo queryLatest() {
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) {
            return null;
        }
        return meta.latest;
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final VersionsInfo oldVersion, final @NotNull VersionsInfo newVersion) {
        if (oldVersion == null) {
            return new EmbedBuilder()
                .setDescription("New Minecraft Version Available!")
                .setColor(Color.CYAN)
                .setDescription(newVersion.snapshot());
        }

        VersionType versionType = getVersionType(oldVersion, newVersion);
        String version = versionType == VersionType.RELEASE ? newVersion.release() : newVersion.snapshot();
        String changelogUrl = versionType.getChangelogUrl(version);

        return new EmbedBuilder()
            .setTitle(versionType.getDisplay() + " Available!")
            .setDescription(version + "\nChangelog: " + changelogUrl)
            .setColor(versionType.getColor());
    }

    private VersionType getVersionType(VersionsInfo oldVersion, VersionsInfo newVersion) {
        if (!oldVersion.release().equals(newVersion.release())) {
            return VersionType.RELEASE;
        } else if (newVersion.snapshot().contains("-release-candidate-")) {
            return VersionType.RELEASE_CANDIDATE;
        } else if (newVersion.snapshot().contains("-pre")) {
            return VersionType.PRE_RELEASE;
        } else {
            return VersionType.SNAPSHOT;
        }
    }

    private enum VersionType {
        RELEASE("New Minecraft Release", Color.GREEN, "java-edition-%s"),
        RELEASE_CANDIDATE("New Minecraft Release Candidate", Color.PINK, "%s"),
        PRE_RELEASE("New Minecraft Pre-Release", Color.ORANGE, "%s"),
        SNAPSHOT("New Minecraft Snapshot", Color.CYAN, "%s");

        private final String display;
        private final Color color;
        private final String urlPath;

        VersionType(String display, Color color, String urlPath) {
            this.display = display;
            this.color = color;
            this.urlPath = urlPath;
        }

        public String getDisplay() {
            return display;
        }

        public Color getColor() {
            return color;
        }

        public String getChangelogUrl(String version) {
            return CHANGELOG_BASE_URL + String.format(urlPath, version.replace('.', '-'));
        }
    }
}
