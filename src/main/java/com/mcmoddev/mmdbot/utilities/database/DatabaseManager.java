/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.utilities.database;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.TimestampedConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.time.ZoneOffset;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Manager type for the backing SQL database for MMDBot.
 *
 * <p>The database manager makes use of {@link Jdbi} for accessing the database through fluent or DAO-based APIs, and
 * {@link Flyway} to automatically migrate the database to the schemas that this version of the bot understands.</p>
 *
 * @author Antoine Gagnon
 * @author sciwhiz12
 */
public class DatabaseManager {
    /**
     * The data source connected to the database which is used by the application.
     */
    private final DataSource dataSource;

    /**
     * The JDBI instance linked to the {@linkplain #dataSource database}.
     */
    private final Jdbi jdbi;

    /**
     * Creates a {@code DatabaseManager} by creating a {@link SQLiteDataSource} pointing at the SQLite database
     * specified by the URL.
     *
     * @param url the url of the SQLite database to connect to
     * @return a database manager connected to the specifiedSQLite database
     * @throws IllegalArgumentException if the URL does not start with the {@code jdbc:sqlite:} prefix
     */
    public static DatabaseManager connectSQLite(final String url) {
        checkArgument(url.startsWith("jdbc:sqlite:"), "SQLite DB URL does not start with 'jdbc:sqlite:': %s", url);

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setDatabaseName("mmdbot");

        return new DatabaseManager(dataSource);
    }

    /**
     * Constructs a {@code DatabaseManager} using the provided data source. {@linkplain Flyway#migrate() Flyway
     * migration} is invoked on the database. Note that the database's connection returned by the data source must not
     * require any username or password to connect.
     *
     * @param dataSource the SQL data source
     */
    public DatabaseManager(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbi = Jdbi.create(dataSource);

        // Install the SQL Objects and Guava plugins
        jdbi.installPlugin(new SqlObjectPlugin());
        // Set default timezone to UTC
        jdbi.getConfig(TimestampedConfig.class).setTimezone(ZoneOffset.UTC);

        // Perform Flyway migration on the database
        final Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
    }

    /**
     * {@return the SQL data source}
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * {@return the JDBI instance linked to the database this manager is connected to}
     */
    public Jdbi jdbi() {
        return jdbi;
    }
}
