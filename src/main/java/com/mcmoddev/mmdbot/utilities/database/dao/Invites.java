package com.mcmoddev.mmdbot.utilities.database.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface Invites extends Transactional<Invites> {

    /// Insertion methods ///

    @SqlUpdate("insert into invites values (:name, :link)")
    void insert(@Bind("name") String name, @Bind("link") String link);

    @SqlQuery("select name from invites")
    List<String> getAllNames();

    /// Query methods ///

    @SqlQuery("select link from invites where name = :name")
    Optional<String> getLink(@Bind("name") String name);

    @SqlUpdate("delete from invites where name = :name")
    void delete(@Bind("name") String name);

}
