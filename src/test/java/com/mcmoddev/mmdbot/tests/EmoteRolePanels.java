package com.mcmoddev.mmdbot.tests;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jetbrains.annotations.Nullable;

public interface EmoteRolePanels {

    @Nullable
    @SqlQuery("select role from role_panels where channel = :channel and message = :message and emote = :emote")
    Long getRole(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

    @Nullable
    @SqlQuery("select permanent from role_panels where channel = :channel and message = :message and emote = :emote")
    Boolean isPermanent(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

}
