package com.mcmoddev.mmdbot.commander.util.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Nullable;

public interface UpdateNotifiersDAO {
    @Nullable
    @SqlQuery("select latestVersion from update_notifiers where notifier = :notifier")
    String getLatest(@Bind("notifier") String notifier);

    @SqlUpdate("insert into update_notifiers values(:notifier, :latestVersion)")
    void setLatest(@Bind("notifier") String notifier, @Bind("latestVersion") String latestVersion);
}
