/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.utilities.updatenotifiers.forge;

import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.TimerTask;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.NOTIFIER_FORGE;

/**
 * The type Forge update notifier.
 *
 * @author Antoine Gagnon
 */
public final class ForgeUpdateNotifier extends TimerTask {

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
        try {
            LOGGER.debug(NOTIFIER_FORGE, "Checking for new Forge versions...");
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
                    embed.addField("Latest", String.format("**%s** -> **%s**%n", lastForgeVersions.getLatest(),
                        latest.getLatest()), true);
                    embed.setDescription(Utils.makeHyperlink(CHANGELOG, String.format(CHANGELOG_URL_TEMPLATE, mcVersion,
                        latest.getLatest())));
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
                    embed.addField("Recommended", String.format("*none* -> **%s**%n", latest.getRecommended()),
                        true);
                    embed.setDescription(Utils.makeHyperlink(CHANGELOG, String.format(CHANGELOG_URL_TEMPLATE,
                        mcVersion, latest.getRecommended())));
                    changed = true;
                    logMsg.append("Recommended, from none to ").append(latest.getLatest());
                } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                    embed.addField("Recommended", String.format("**%s** -> **%s**%n",
                        lastForgeVersions.getRecommended(), latest.getRecommended()), true);
                    embed.setDescription(Utils.makeHyperlink(CHANGELOG, String.format(CHANGELOG_URL_TEMPLATE,
                        mcVersion, latest.getRecommended())));
                    changed = true;
                    logMsg.append("Recommended, from ").append(lastForgeVersions.getLatest()).append(" to ")
                        .append(latest.getLatest());
                }
            }

            if (changed) {
                LOGGER.info(NOTIFIER_FORGE, "New Forge version found for {}: {}", mcVersion, logMsg);
                // TODO: Save this to disk to persist on restarts
                lastForgeVersions = latest;

                Utils.getChannelIfPresent(getConfig().getChannel("notifications.forge"),
                    channel -> channel.sendMessageEmbeds(embed.build()).queue());
            } else {
                LOGGER.debug(NOTIFIER_FORGE, "No new Forge version found");
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(NOTIFIER_FORGE, "Error while running", ex);
            ex.printStackTrace();
        }
    }
}
