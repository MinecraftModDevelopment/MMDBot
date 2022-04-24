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
package com.mcmoddev.mmdbot.watcher.event;

import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.database.PersistedRoles;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class PersistedRolesEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent event) {
        TheWatcher.database().useExtension(PersistedRoles.class, db -> {
            final var notPers = TheWatcher.getInstance().getConfig().roles().getNotPersisted().stream().map(SnowflakeValue::asLong).toList();
            final var roles = db.getRoles(event.getUser().getIdLong(), event.getGuild().getIdLong()).stream()
                .filter(Predicate.not(notPers::contains))
                .map(event.getGuild()::getRoleById)
                .filter(Objects::nonNull)
                .toList();
            if (!roles.isEmpty()) {
                event.getGuild()
                    .modifyMemberRoles(event.getMember(), roles)
                    .reason("Persisted roles")
                    .queue();
            }
            db.clear(event.getUser().getIdLong(), event.getGuild().getIdLong());
        });
    }

    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        if (event.getMember() == null) return;
        TheWatcher.database().useExtension(PersistedRoles.class, db -> {
            db.clear(event.getUser().getIdLong(), event.getGuild().getIdLong());
            final var notPers = TheWatcher.getInstance().getConfig().roles().getNotPersisted().stream().map(SnowflakeValue::asLong).toList();
            final var roles = event.getMember().getRoles()
                .stream()
                .filter(r -> !r.isManaged() && event.getGuild().getSelfMember().canInteract(r))
                .map(ISnowflake::getIdLong)
                .filter(Predicate.not(notPers::contains))
                .toList();
            if (!roles.isEmpty()) {
                db.insert(event.getUser().getIdLong(), event.getGuild().getIdLong(), roles);
            }
        });
    }

}
