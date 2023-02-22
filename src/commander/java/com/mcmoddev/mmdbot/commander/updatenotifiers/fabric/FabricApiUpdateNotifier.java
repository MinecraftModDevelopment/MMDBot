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
package com.mcmoddev.mmdbot.commander.updatenotifiers.fabric;

import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifier;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Fabric API update notifier.
 *
 * @author williambl
 * @author matyrobbrt
 */
public final class FabricApiUpdateNotifier extends UpdateNotifier<String> {

    public FabricApiUpdateNotifier() {
        super(NotifierConfiguration.<String>builder()
            .name("fabricapi")
            .channelGetter(Configuration.Channels.UpdateNotifiers::fabric)
            .serializer(StringSerializer.SELF)
            .versionComparator(NotifierConfiguration.notEqual())
            .webhookInfo(new WebhookInfo("Fabric Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125360129265734/unknown.png"))
            .build());
    }

    @Nullable
    @Override
    protected String queryLatest() {
        return FabricVersionHelper.getLatestApi();
    }

    @Override
    protected @NotNull EmbedBuilder getEmbed(@Nullable final String oldVersion, @Nonnull final String newVersion) {
        return new EmbedBuilder()
            .setTitle("New Fabric API release available!")
            .setDescription(newVersion)
            .setColor(0xDBD2B5);
    }
}
