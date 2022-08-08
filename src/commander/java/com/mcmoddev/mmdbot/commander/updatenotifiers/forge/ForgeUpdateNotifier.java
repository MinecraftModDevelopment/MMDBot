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
package com.mcmoddev.mmdbot.commander.updatenotifiers.forge;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * The Forge update notifier.
 *
 * @author Antoine Gagnon
 * @author matyrobbrt
 */
public final class ForgeUpdateNotifier extends UpdateNotifier<MinecraftForgeVersion> {

    /**
     * The changelog URL template
     */
    private static final String CHANGELOG_URL_TEMPLATE
        = "https://maven.minecraftforge.net/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";

    public ForgeUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftForgeVersion>builder()
            .name("forge")
            .channelGetter(Configuration.Channels.UpdateNotifiers::forge)
            .versionComparator(NotifierConfiguration.notEqual())
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, MinecraftForgeVersion.class))
            .webhookInfo(new WebhookInfo("Forge Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125547430096966/unknown.png"))
            .build());
    }

    @Override
    protected @NotNull MinecraftForgeVersion queryLatest() throws IOException {
        return ForgeVersionHelper.getLatestMcVersionForgeVersions();
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final MinecraftForgeVersion oldVersion, final @NotNull MinecraftForgeVersion newVersion) {
        final var embed = new EmbedBuilder();
        embed.addField("Minecraft Version", newVersion.mcVersion(), true);
        embed.setTitle("Forge version update");
        embed.setColor(Color.ORANGE);

        final var mcVersion = newVersion.mcVersion();
        final var latest = newVersion.forgeVersion();

        if (oldVersion == null || !oldVersion.mcVersion().equals(newVersion.mcVersion())) {
            embed.addField("Version", latest.getLatest(), true);
            addChangelog(embed, mcVersion, latest.getLatest(), mcVersion, latest.getLatest());
            return embed;
        }

        final var lastForgeVersions = oldVersion.forgeVersion();
        if (latest.getLatest() != null && !lastForgeVersions.getLatest().equals(latest.getLatest())) {
            final var start = lastForgeVersions.getLatest();
            final var end = latest.getLatest();
            embed.addField("Latest", String.format("**%s** -> **%s**%n", start, end), true);
            addChangelog(embed, mcVersion, start, mcVersion, end);
        }

        if (latest.getRecommended() != null) {
            if (lastForgeVersions.getRecommended() == null) {
                final var version = latest.getRecommended();
                embed.addField("Recommended", String.format("*none* -> **%s**%n", version),
                    true);
                embed.setDescription(MarkdownUtil.maskedLink("Changelog", String.format(CHANGELOG_URL_TEMPLATE,
                    mcVersion, latest.getRecommended())));
            } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                final var start = lastForgeVersions.getRecommended();
                final var end = latest.getRecommended();
                embed.addField("Recommended", String.format("**%s** -> **%s**%n", start, end), true);
                addChangelog(embed, mcVersion, start, mcVersion, end);
            }
        }
        return embed;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, String mcStart, String forgeStart, String mcEnd, String forgeEnd) {
        try {
            final var changelog = getChangelogBetweenVersions(
                mcStart, forgeStart, mcEnd, forgeEnd
            );
            embedBuilder.setDescription("""
                [Changelog](%s):
                ```
                %s
                ```""".formatted(
                CHANGELOG_URL_TEMPLATE.formatted(mcEnd, forgeEnd), changelog
            ));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(final String startMc, final String startForge, final String endMc, final String endForge) throws IOException {
        final var startUrl = new URL(CHANGELOG_URL_TEMPLATE.formatted(startMc, startForge));
        final var endUrl = new URL(CHANGELOG_URL_TEMPLATE.formatted(endMc, endForge));

        final var startMcVersionSplit = startMc.split("\\.");
        final var startForgeVersionSplit = startForge.split("\\.");
        final var startChangelog = getUrlAsString(startUrl).replace("""
            %s.%s.x Changelog
            %s.%s
            ====""".formatted(startMcVersionSplit[0], startMcVersionSplit[1], startForgeVersionSplit[0], startForgeVersionSplit[1]), "");

        final var endChangelog = getUrlAsString(endUrl);
        var changelog = endChangelog.replace(startChangelog, "");

        final var endMcVersionSplit = endMc.split("\\.");
        final var endForgeVersionSplit = endForge.split("\\.");
        changelog = changelog.replace("""
            %s.%s.x Changelog
            %s.%s
            ====""".formatted(endMcVersionSplit[0], endMcVersionSplit[1], endForgeVersionSplit[0], endForgeVersionSplit[1]), "");

        return changelog;
    }

    public static String getUrlAsString(URL u) throws IOException {
        try (final var in = u.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
