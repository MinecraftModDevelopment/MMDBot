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
package com.mcmoddev.mmdbot.commander.util.dao;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ComChannelsDAO extends Transactional<ComChannelsDAO> {
    @SqlUpdate("insert into community_channels (id, owner) values (:id, :owner)")
    void insert(@Bind("id") long channelId, @Bind("owner") long ownerId);

    @Nullable
    @CanIgnoreReturnValue
    default Long changeOwnership(long channelId, long newOwner) {
        final var oldOwner = getOwner(channelId);
        deleteEntry(channelId);
        insert(channelId, newOwner);
        return oldOwner;
    }

    @Nullable
    @SqlQuery("select owner from community_channels where id = :id")
    Long getOwner(@Bind("id") long channelId);

    @Nullable
    @SqlQuery("select ignore_archival_until from community_channels where id = :id")
    Long getIgnoreUntil(@Bind("id") long channelId);

    @SqlUpdate("update community_channels set ignore_archival_until = :until where id = :id")
    void setIgnoreUntil(@Bind("id") long channelId, @Bind("until") long ignoreUntil);

    @SqlQuery("select saved_from_archival from community_channels where id = :id")
    boolean wasPreviouslySaved(@Bind("id") long channelId);

    @SqlUpdate("update community_channels set saved_from_archival = :saved where id = :id")
    void setPreviouslySaved(@Bind("id") long channelId, @Bind("saved") boolean prevSaved);

    @SqlUpdate("delete from community_channels where id = :id")
    void deleteEntry(@Bind("id") long channelId);

    default void removeIgnoreUntil(long channelId) {
        final var owner = Objects.requireNonNull(getOwner(channelId));
        final var prevSaved = wasPreviouslySaved(channelId);
        deleteEntry(channelId);
        insert(channelId, owner);
        setPreviouslySaved(channelId, prevSaved);
    }

    default void removeExtraData(long channelId) {
        final var owner = Objects.requireNonNull(getOwner(channelId));
        deleteEntry(channelId);
        insert(channelId, owner);
    }

}
