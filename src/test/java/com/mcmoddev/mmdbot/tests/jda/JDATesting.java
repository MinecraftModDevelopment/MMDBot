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
package com.mcmoddev.mmdbot.tests.jda;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JDATesting {
    private static final long SELF_USER_ID = 1;
    private static final long GUILD_ID = 2;

    private static final long TEXT_CHANNEL_ID = 3;

    public static JDAImpl jda;
    public static GuildImpl guild;
    public static TextChannel textChannel;

    @Test
    void entitiesMatch() {
        assertEquals(
            SELF_USER_ID, jda.getSelfUser().getIdLong(), "Self User ID"
        );
        assertEquals(
            textChannel, guild.getChannelById(TextChannel.class, TEXT_CHANNEL_ID), "Text Channel"
        );
    }

    @BeforeAll
    public static void setup() {
        if (jda != null) return;
        jda = mock(JDAImpl.class);
        when(jda.getCacheFlags()).thenReturn(EnumSet.noneOf(CacheFlag.class));

        final var selfUser = spy(new SelfUserImpl(SELF_USER_ID, jda));
        when(jda.getSelfUser()).thenReturn(selfUser);

        guild = spy(new GuildImpl(jda, GUILD_ID));
        when(guild.getIdLong()).thenReturn(GUILD_ID);

        textChannel = spy(new TextChannelImpl(TEXT_CHANNEL_ID, guild));
        when(guild.getChannelById(TextChannel.class, TEXT_CHANNEL_ID)).thenReturn(textChannel);
    }
}
