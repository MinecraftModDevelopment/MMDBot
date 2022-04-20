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

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.LOGGER;
import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.MARKER;

/**
 * The type Forge update notifier.
 *
 * @author Antoine Gagnon
 */
public final class ForgeUpdateNotifier implements Runnable {

    /**
     * The constant CHANGELOG.
     */
    private static final String CHANGELOG = "Changelog";

    /**
     * The constant CHANGELOG_URL_TEMPLATE.
     */
    private static final String CHANGELOG_URL_TEMPLATE
        = "https://maven.minecraftforge.net/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";

    /**
     * The Mc version.
     */
    private String mcVersion;

    /**
     * The Last forge versions.
     */
    private ForgeVersion lastForgeVersions;

    /**
     * Instantiates a new Forge update notifier.
     *
     * @throws IOException the io exception
     */
    public ForgeUpdateNotifier() throws IOException {
        final MinecraftForgeVersion mcForgeVersions = ForgeVersionHelper.getLatestMcVersionForgeVersions();
        mcVersion = mcForgeVersions.getMcVersion();
        lastForgeVersions = mcForgeVersions.getForgeVersion();
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        if (TheCommander.getInstance() == null) {
            LOGGER.warn("Cannot check for new Forge versions, due to the bot instance being null.");
            return;
        }
        try {
            LOGGER.debug(MARKER, "Checking for new Forge versions...");
            mcVersion = ForgeVersionHelper.getLatestMcVersionForgeVersions().getMcVersion();

            final var latest = ForgeVersionHelper.getForgeVersionsForMcVersion(mcVersion);

            var changed = false;
            final var embed = new EmbedBuilder();
            embed.addField("Minecraft Version", mcVersion, true);
            embed.setTitle("Forge version update");
            embed.setColor(Color.ORANGE);
            embed.setTimestamp(Instant.now());

            final var logMsg = new StringBuilder(32);
            if (latest.getLatest() != null) {
                if (lastForgeVersions.getLatest() == null) {
                    embed.addField("Latest", String.format("*none* -> **%s**%n", latest.getLatest()),
                        true);
                    embed.setDescription(Utils.makeHyperlink(CHANGELOG, String.format(CHANGELOG_URL_TEMPLATE,
                        mcVersion, latest.getLatest())));
                    changed = true;
                    logMsg.append("Latest, from none to ").append(latest.getLatest());
                } else if (!latest.getLatest().equals(lastForgeVersions.getLatest())) {
                    final var start = lastForgeVersions.getLatest();
                    final var end = latest.getLatest();
                    embed.addField("Latest", String.format("**%s** -> **%s**%n", start,
                        end), true);
                    embed.setDescription("""
                        [Changelog](%s):
                        ```
                        %s
                        ```""".formatted(CHANGELOG_URL_TEMPLATE.formatted(mcVersion, end),
                        getChangelogBetweenVersions(mcVersion, start, mcVersion, end)));
                    changed = true;
                    logMsg.append("Latest, from ").append(lastForgeVersions.getLatest()).append(" to ")
                        .append(latest.getLatest());
                }
            }

            if (latest.getRecommended() != null) {
                if (logMsg.length() != 0) {
                    logMsg.append("; ");
                }
                if (lastForgeVersions.getRecommended() == null) {
                    final var version = latest.getRecommended();
                    embed.addField("Recommended", String.format("*none* -> **%s**%n", version),
                        true);
                    embed.setDescription(Utils.makeHyperlink(CHANGELOG, String.format(CHANGELOG_URL_TEMPLATE,
                        mcVersion, latest.getRecommended())));
                    changed = true;
                    logMsg.append("Recommended, from none to ").append(latest.getLatest());
                } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                    final var start = lastForgeVersions.getRecommended();
                    final var end = latest.getRecommended();
                    embed.addField("Recommended", String.format("**%s** -> **%s**%n",
                        start, end), true);
                    embed.setDescription("""
                        [Changelog](%s):
                        ```
                        %s
                        ```""".formatted(CHANGELOG_URL_TEMPLATE.formatted(mcVersion, end),
                        getChangelogBetweenVersions(mcVersion, start, mcVersion, end)));
                    changed = true;
                    logMsg.append("Recommended, from ").append(lastForgeVersions.getLatest()).append(" to ")
                        .append(latest.getLatest());
                }
            }

            if (changed) {
                LOGGER.info(MARKER, "New Forge version found for {}: {}", mcVersion, logMsg);
                lastForgeVersions = latest;

                TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers().forge().forEach(chId -> {
                    final var channel = chId.resolve(id -> TheCommander.getJDA().getChannelById(TextChannel.class, id));
                    if (channel != null) {
                        channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                            if (channel.getType() == ChannelType.NEWS) {
                                msg.crosspost().queue();
                            }
                        });
                    }
                });
            } else {
                LOGGER.debug(MARKER, "No new Forge version found");
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(MARKER, "Error while running", ex);
            ex.printStackTrace();
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
