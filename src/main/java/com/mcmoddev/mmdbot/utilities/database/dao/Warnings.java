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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access object for warnings.
 *
 * @author matyrobbrt
 */
public interface Warnings extends Transactional<Warnings> {

    /// Insertion methods ///

    /**
     * @deprecated Use the version which generates a random UUID to be inserted
     */
    @Deprecated
    @SqlUpdate("insert or ignore into warnings values (:user, :guild, :warn_id, :reason, :moderator, :timestamp)")
    void insert(@Bind("user") long user, @Bind("guild") long guild, @Bind("warn_id") String warnId, @Bind("reason") String reason, @Bind("moderator") long moderator, @Bind("timestamp") Instant timestamp);

    /**
     * Inserts a new warning in the database
     *
     * @param user      the user to warn
     * @param guild     the guild in which to warn
     * @param reason    the reason to warn
     * @param moderator the moderator who warned
     * @param timestamp the time at which the warning "happened"
     * @return the warning UUID
     */
    default UUID insert(long user, long guild, String reason, long moderator, Instant timestamp) {
        final var id = UUID.randomUUID();
        insert(user, guild, id.toString(), reason, moderator, timestamp);
        return id;
    }

    /// Query methods ///

    @SqlQuery("select warn_id from warnings where user_id = :user and guild_id = :guild")
    List<String> getWarningsForUser(@Bind("user") long user, @Bind("guild") long guild);

    @SqlQuery("select warn_id from warnings")
    List<String> getAllWarnings();

    @SqlQuery("select user_id from warnings where warn_id = :id")
    long getUser(@Bind("id") String warnId);

    @SqlQuery("select guild_id from warnings where warn_id = :id")
    long getGuild(@Bind("id") String warnId);

    @SqlQuery("select reason from warnings where warn_id = :id")
    String getReason(@Bind("id") String warnId);

    @SqlQuery("select reason from warnings where warn_id = :id")
    Optional<String> getReasonOptional(@Bind("id") String warnId);

    default boolean warningExists(String warnId) {
        return getReasonOptional(warnId).isPresent();
    }

    @SqlQuery("select timestamp from warnings where warn_id = :id")
    Instant getTimestamp(@Bind("id") String warnId);

    @SqlQuery("select moderator from warnings where warn_id = :id")
    long getModerator(@Bind("id") String warnId);

    /// Deletion methods ///

    @SqlUpdate("delete from warnings where user_id = :user and guild_id = :guild")
    void clearAll(@Bind("user") long userId, @Bind("guild") long guildId);

    @SqlUpdate("delete from warnings where warn_id = :id")
    void deleteById(@Bind("id") String warnId);

    default WarningDocument getWarningDocument(final String warnId) {
        if (!warningExists(warnId)) {
            return null;
        }
        final var userId = getUser(warnId);
        final long guildId = getGuild(warnId);
        final var reason = getReason(warnId);
        final var moderator = getModerator(warnId);
        final var timestamp = getTimestamp(warnId);
        return new WarningDocument(userId, guildId, warnId, reason, moderator, timestamp);
    }

    record WarningDocument(long userId, long guildId, String warnId, String reason, long moderatorId,
                           Instant timestamp) {
    }
}
