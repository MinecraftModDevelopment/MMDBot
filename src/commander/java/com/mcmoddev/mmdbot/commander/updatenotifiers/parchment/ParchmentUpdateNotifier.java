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
package com.mcmoddev.mmdbot.commander.updatenotifiers.parchment;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.updatenotifiers.parchment.ParchmentVersionHelper.ParchmentVersion;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Comparator;

/**
 * The Parchment update notifier.
 *
 * @author matyrobbrt
 */
public final class ParchmentUpdateNotifier extends UpdateNotifier<ParchmentVersion> {

    public ParchmentUpdateNotifier() {
        super(NotifierConfiguration.<ParchmentVersion>builder()
            .name("parchment")
            .channelGetter(Configuration.Channels.UpdateNotifiers::parchment)
            .versionComparator(Comparator.comparing(ParchmentVersion::getDate))
            .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, ParchmentVersion.class))
            .webhookInfo(new WebhookInfo("Parchment Updates", "https://media.discordapp.net/attachments/957353544493719632/1006189498960466010/unknown.png"))
            .build());
    }

    @Nullable
    @Override
    protected ParchmentVersion queryLatest() {
        return ParchmentVersionHelper.newest(ParchmentVersionHelper.byMcReleases());
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final ParchmentVersion oldVersion, final @NotNull ParchmentVersion newVersion) {
        return new EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("A new %s Parchment version is available!".formatted(newVersion.mcVersion()))
            .addField("Version", newVersion.parchmentVersion(), false)
            .addField("Coordinate", "`org.parchmentmc.data:parchment-%s:%s`".formatted(newVersion.mcVersion(), newVersion.parchmentVersion()), false);
    }
}
