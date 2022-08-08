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
package com.mcmoddev.mmdbot.watcher.util.database;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

import java.util.List;

/**
 * Data access object for the role persistence table.
 * TODO make them work after the bot split
 *
 * @author sciwhiz12
 */
public interface PersistedRoles extends Transactional<PersistedRoles> {

    /// Insertion methods ///

    /**
     * Inserts an entry for the given user and role into the table.
     *
     * @param userId  the snowflake ID of the user
     * @param roleId  the snowflake ID of the role
     * @param guildId the snowflake ID of the guild
     */
    @SqlUpdate("insert into role_persist values (:user, :role, :guild)")
    void insert(@Bind("user") long userId, @Bind("role") long roleId, @Bind("guild") long guildId);

    /**
     * Inserts an entry for each role in the given iterable with the given user into the table.
     *
     * @param userId  the snowflake ID of the user
     * @param guildId the snowflake ID of the guild
     * @param roles   an iterable of snowflake IDs of roles
     */
    default void insert(long userId, long guildId, Iterable<Long> roles) {
        roles.forEach(roleId -> insert(userId, roleId, guildId));
    }

    /// Query methods ///

    /**
     * Gets the stored roles for the given user.
     *
     * @param userId  the snowflake ID of the user
     * @param guildId the snowflake ID of the guild
     * @return the list of role snowflake IDs which are associated with the user
     */
    @SqlQuery("select role_id from role_persist where user_id = :user and guild_id = :guild")
    List<Long> getRoles(@Bind("user") long userId, @Bind("guild") long guildId);

    /// Deletion methods ///

    /**
     * Delete the entry for the given user and role from the table.
     *
     * @param userId  the snowflake ID of the user
     * @param roleId  the snowflake ID of the role
     * @param guildId the snowflake ID of the guild
     */
    @SqlUpdate("delete from role_persist where user_id = :user and role_id = :role and guild_id = :guild")
    void delete(@Bind("user") long userId, @Bind("role") long roleId, @Bind("guild") long guildId);

    /**
     * Clears all entries for the given user from the table.
     *
     * @param userId  the snowflake ID of the user
     * @param guildId the snowflake ID of the guild
     */
    @SqlUpdate("delete from role_persist where user_id = :user and guild_id = :guild")
    void clear(@Bind("user") long userId, @Bind("guild") long guildId);
}
