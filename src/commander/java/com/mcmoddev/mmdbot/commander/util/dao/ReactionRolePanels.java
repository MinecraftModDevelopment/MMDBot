package com.mcmoddev.mmdbot.commander.util.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jetbrains.annotations.Nullable;

public interface ReactionRolePanels {

    @Nullable
    @SqlQuery("select role from role_panels where channel = :channel and message = :message and emote = :emote")
    Long getRole(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

    @Nullable
    @SqlQuery("select permanent from role_panels where channel = :channel and message = :message and emote = :emote")
    Boolean isPermanent(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

    @SqlUpdate("insert into role_panels values(:channel, :message, :emote, :role, :perm)")
    void insert(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote, @Bind("role") long role, @Bind("perm") boolean permanent);
}
