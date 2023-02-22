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
package com.mcmoddev.mmdbot.watcher.punishments;

import com.mcmoddev.mmdbot.watcher.util.Configuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class NewAccount implements PunishableAction<GuildMemberJoinEvent> {
    @Override
    public Punishment getPunishment(final Configuration.Punishments config) {
        return config.newAccount;
    }

    @Override
    public Class<GuildMemberJoinEvent> getEventClass() {
        return GuildMemberJoinEvent.class;
    }

    @Override
    public @Nullable Member getPunishedMember(final GuildMemberJoinEvent event) {
        return event.getMember();
    }

    @Override
    public String getReason(final GuildMemberJoinEvent event, final Member member) {
        return "Account too new";
    }

    @Override
    public boolean test(final GuildMemberJoinEvent event) {
        return !event.getUser().isBot() && event.getMember()
            .getTimeCreated()
            .toInstant()
            .isAfter(Instant.now() // TODO Make the threshold a config
                .minus(1, ChronoUnit.HOURS));
    }
}
