package com.mcmoddev.mmdbot.commander.util.dao;

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
