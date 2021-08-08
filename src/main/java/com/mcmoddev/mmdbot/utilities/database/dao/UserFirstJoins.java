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
