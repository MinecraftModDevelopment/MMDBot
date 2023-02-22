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
package com.mcmoddev.mmdbot.painter.util.dao;

import net.dv8tion.jda.api.entities.ISnowflake;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface DayCounterDAO extends Transactional<DayCounterDAO> {
    @SqlQuery("select backwards from day_counter where guild_id = :guild")
    @Nullable Boolean $isBackwards(@Bind("guild") ISnowflake guild);

    default boolean isBackwards(ISnowflake guild) {
        return Objects.requireNonNullElse($isBackwards(guild), false);
    }

    @SqlQuery("select current_day from day_counter where guild_id = :guild")
    @Nullable Integer $getCurrentDay(@Bind("guild") ISnowflake guild);

    default int getCurrentDay(ISnowflake guild) {
        return Objects.requireNonNullElse($getCurrentDay(guild), 0);
    }

    @SqlUpdate("insert or replace into day_counter (guild_id, current_day, backwards) values (:guild, :day, :backwards)")
    void update(@Bind("guild") ISnowflake guild, @Bind("day") int day, @Bind("backwards") boolean backwards);
}
