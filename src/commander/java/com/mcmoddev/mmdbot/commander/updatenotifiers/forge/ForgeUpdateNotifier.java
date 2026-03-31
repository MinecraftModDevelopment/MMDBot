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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.SharedVersionHelpers;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public final class ForgeUpdateNotifier extends UpdateNotifier<MinecraftForgeVersions> {

    /**
     * The changelog URL template
     */
    private static final String CHANGELOG_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";

    public ForgeUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftForgeVersions>builder()
            .name("forge")
            .channelGetter(Configuration.Channels.UpdateNotifiers::forge)
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, MinecraftForgeVersions.class))
            .versionComparator(NotifierConfiguration.notEqual())
            .webhookInfo(new WebhookInfo("Forge Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125547430096966/unknown.png"))
            .build());
    }

    @Override
    protected @NotNull MinecraftForgeVersions queryLatest() {
        return new MinecraftForgeVersions(ForgeVersionHelper.getForgeVersions());
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final MinecraftForgeVersions oldVersion, final MinecraftForgeVersions newVersion) {
        final String version;
        if (oldVersion == null) {
            version = newVersion.byMcVersion().entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .orElseThrow()
                .getValue();
        } else {
            version = newVersion.byMcVersion().entrySet().stream()
                .filter(entry -> !Objects.equals(oldVersion.byMcVersion().get(entry.getKey()), entry.getValue()))
                .max(Map.Entry.comparingByKey())
                .orElseThrow()
                .getValue();
        }

        final String mcVersion = version.split("-")[0];

        final var embed = new EmbedBuilder();
        embed.addField("Minecraft Version", mcVersion, true);
        embed.setTitle("Forge version update");
        embed.setColor(0x0000FF);

        final String oldForgeVersion = oldVersion == null ? null : oldVersion.byMcVersion().get(mcVersion);
        if (oldForgeVersion == null) {
            embed.addField("Version", version, true);
        } else {
            boolean isNoLongerBeta = isNoLongerBeta(oldForgeVersion, version);
            embed.addField(isNoLongerBeta ? "New stable release" : "Latest", "**%s** -> **%s**".formatted(oldForgeVersion, version), true);
        }

        addChangelog(embed, oldForgeVersion, version);

        return embed;
    }

    private static boolean isNoLongerBeta(String oldForgeVersionFull, String newForgeVersionFull) {
        try {
            final String oldForgeVersion = oldForgeVersionFull.substring(oldForgeVersionFull.indexOf('-') + 1);
            final String newForgeVersion = newForgeVersionFull.substring(newForgeVersionFull.indexOf('-') + 1);

            final String[] oldVersionParts = oldForgeVersion.split("\\.");
            final String[] newVersionParts = newForgeVersion.split("\\.");

            if (oldVersionParts.length > 1 && newVersionParts.length > 1) {
                // The second part of the version number indicates beta status. '0' is beta.
                boolean wasBeta = oldVersionParts[1].equals("0");
                boolean isNowStable = !newVersionParts[1].equals("0");
                return wasBeta && isNowStable;
            }
        } catch (Exception e) {
            // If any parsing error occurs (e.g., unexpected version format),
            // safely assume it's not a beta-to-stable transition.
            return false;
        }
        return false;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable String forgeStart, String forgeEnd) {
        try {
            String changelog = getChangelogBetweenVersions(
                forgeStart, forgeEnd
            );
            if (changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "MinecraftForge/Forge");

            embedBuilder.setDescription(Utils.truncate("""
                [Changelog](%s):
                %s
                """.formatted(
                CHANGELOG_URL.formatted(forgeEnd), changelog
            ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(@Nullable final String forgeStart, final String forgeEnd) throws IOException {
        if (forgeStart == null || forgeStart.equals(forgeEnd)) {
            final String[] split = getUrlAsString(new URL(CHANGELOG_URL.formatted(forgeEnd))).split("\n");
            final StringBuilder changelog = new StringBuilder(split[0])
                .append('\n');
            for (int i = 1; i < split.length; i++) {
                // new version detected
                if (split[i].startsWith(" - ")) break;
                changelog.append(split[i]).append('\n');
            }
            return changelog.toString();
        }

        final var startUrl = new URL(CHANGELOG_URL.formatted(forgeStart));
        final var endUrl = new URL(CHANGELOG_URL.formatted(forgeEnd));
        final var startChangelog = getUrlAsString(startUrl);

        final var endChangelog = getUrlAsString(endUrl);

        return endChangelog.replace(startChangelog, "");
    }

    public static String getUrlAsString(URL u) throws IOException {
        try (final var in = u.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
