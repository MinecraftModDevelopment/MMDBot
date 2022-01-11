/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.utilities.database.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Data access object for the user first join times table.
 *
 * @author sciwhiz12
 */
public interface UserFirstJoins extends Transactional<UserFirstJoins> {

    /// Insertion methods ///

    /**
     * Inserts the given join timestamp for the given user into the table.
     *
     * @param user      the snowflake ID of the user
     * @param timestamp the join timestamp
     */
    @SqlUpdate("insert or ignore into first_join values (:user, :timestamp)")
    void insert(@Bind("user") long user, @Bind("timestamp") Instant timestamp);

    /// Query methods ///

    /**
     * Gets the join timestamp for the given user from the table.
     *
     * @param user the snowflake ID of the user
     * @return an optional containing the join timestamp for the given user
     */
    @SqlQuery("select first_joined_at from first_join where user_id = :user")
    Optional<Instant> get(@Bind("user") long user);
}
