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
package com.mcmoddev.mmdbot.commander.updatenotifiers.quilt;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * The Quilt update notifier.
 *
 * @author KiriCattus
 * @author matyrobbrt
 */
public final class QuiltUpdateNotifier extends UpdateNotifier<String> {

    public QuiltUpdateNotifier() {
        super(NotifierConfiguration.<String>builder()
            .name("quilt-qsl")
            .channelGetter(Configuration.Channels.UpdateNotifiers::quilt)
            .serializer(StringSerializer.SELF)
            .versionComparator(NotifierConfiguration.notEqual())
            .build());
    }

    @Nullable
    @Override
    protected String queryLatest() {
        return QuiltVersionHelper.getQSLVersion();
    }

    @Override
    protected @NotNull EmbedBuilder getEmbed(@Nullable final String oldVersion, final @NotNull String newVersion) {
        return new EmbedBuilder()
            .setTitle("New QSL release available!")
            .setDescription(newVersion)
            .setColor(0xDC29DD);
    }
}
