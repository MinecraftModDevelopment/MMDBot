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
package com.mcmoddev.mmdbot.commander.updatenotifiers.neoforge;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.SharedVersionHelpers;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static com.mcmoddev.mmdbot.commander.updatenotifiers.forge.ForgeUpdateNotifier.getUrlAsString;

/**
 * The NeoForge update notifier.
 *
 * @author matyrobbrt
 */
public final class NeoForgeUpdateNotifier extends UpdateNotifier<NeoForgeVersions> {

    public static final String CHANGELOG_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%1$s/neoforge-%1$s-changelog.txt";

    public NeoForgeUpdateNotifier() {
        super(NotifierConfiguration.<NeoForgeVersions>builder()
            .name("neoforge")
            .channelGetter(Configuration.Channels.UpdateNotifiers::neoforge)
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, NeoForgeVersions.class))
            .versionComparator(NotifierConfiguration.notEqual())
            .webhookInfo(new WebhookInfo("NeoForge Updates", "https://github.com/NeoForged.png"))
            .build());
    }

    @Override
    protected NeoForgeVersions queryLatest() {
        return new NeoForgeVersions(NeoForgeVersionHelper.getNeoForgeVersions());
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final NeoForgeVersions oldVersion, final NeoForgeVersions newVersion) {
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

        final String[] split = version.split("\\.");

        final String mcVersion = "1." + split[0] + "." + split[1];

        final var embed = new EmbedBuilder();
        embed.addField("Minecraft Version", mcVersion, true);
        embed.setTitle("NeoForge version update");
        embed.setColor(Color.YELLOW);

        final String oldNeoVersion = oldVersion == null ? null : oldVersion.byMcVersion().get(mcVersion);
        if (oldNeoVersion == null) {
            embed.addField("Version", version, true);
        } else {
            final boolean isNoLongerBeta = oldNeoVersion.endsWith("-beta") && !version.endsWith("-beta");
            embed.addField(isNoLongerBeta ? "New stable release" : "Latest", "**%s** -> **%s**".formatted(oldNeoVersion, version), true);
        }

        addChangelog(embed, oldNeoVersion, version);

        return embed;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable String neoStart, String neoEnd) {
        try {
            String changelog = getChangelogBetweenVersions(
                neoStart, neoEnd
            );
            if (changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "NeoForged/NeoForge");

            embedBuilder.setDescription(Utils.truncate("""
                [Changelog](%s):
                %s
                """.formatted(
                CHANGELOG_URL.formatted(neoEnd), changelog
            ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(@Nullable final String neoStart, final String neoEnd) throws IOException {
        if (neoStart == null || neoStart.equals(neoEnd)) {
            final String[] split = getUrlAsString(new URL(CHANGELOG_URL.formatted(neoEnd))).split("\n");
            final StringBuilder changelog = new StringBuilder(split[0])
                .append('\n');
            for (int i = 1; i < split.length; i++) {
                // Neo version detected
                if (split[i].startsWith(" - ")) break;
                changelog.append(split[i]).append('\n');
            }
            return changelog.toString();
        }

        final var startUrl = new URL(CHANGELOG_URL.formatted(neoStart));
        final var endUrl = new URL(CHANGELOG_URL.formatted(neoEnd));
        final var startChangelog = getUrlAsString(startUrl);

        final var endChangelog = getUrlAsString(endUrl);

        return endChangelog.replace(startChangelog, "");
    }
}
