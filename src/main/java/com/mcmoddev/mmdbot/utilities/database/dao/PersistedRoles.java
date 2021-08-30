package com.mcmoddev.mmdbot.utilities.database.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

import java.util.List;

/**
 * Data access object for the role persistence table.
 *
 * @author sciwhiz12
 */
public interface PersistedRoles extends Transactional<PersistedRoles> {

    /// Insertion methods ///

    /**
     * Inserts an entry for the given user and role into the table.
     *
     * @param userId the snowflake ID of the user
     * @param roleId the snowflake ID of the role
     */
    @SqlUpdate("insert into role_persist values (:user, :role)")
    void insert(@Bind("user") long userId, @Bind("role") long roleId);

    /**
     * Inserts an entry for each role in the given iterable with the given user into the table.
     *
     * @param userId the snowflake ID of the user
     * @param roles  an iterable of snowflake IDs of roles
     */
    default void insert(long userId, Iterable<Long> roles) {
        roles.forEach(roleId -> insert(userId, roleId));
    }

    /// Query methods ///

    /**
     * Gets the stored roles for the given user.
     *
     * @param userId the snowflake ID of the user
     * @return the list of role snowflake IDs which are associated with the user
     */
    @SqlQuery("select role_id from role_persist where user_id = :user")
    List<Long> getRoles(@Bind("user") long userId);

    /// Deletion methods ///

    /**
     * Delete the entry for the given user and role from the table.
     *
     * @param userId the snowflake ID of the user
     * @param roleId the snowflake ID of the role
     */
    @SqlUpdate("delete from role_persist where user_id = :user and role_id = :role")
    void delete(@Bind("user") long userId, @Bind("role") long roleId);

    /**
     * Clears all entries for the given user from the table.
     *
     * @param userId the snowflake ID of the user
     */
    @SqlUpdate("delete from role_persist where user_id = :user")
    void clear(@Bind("user") long userId);
}