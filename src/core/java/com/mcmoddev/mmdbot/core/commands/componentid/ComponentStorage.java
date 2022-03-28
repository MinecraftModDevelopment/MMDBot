package com.mcmoddev.mmdbot.core.commands.componentid;

import static com.google.common.base.Preconditions.checkArgument;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.TimestampedConfig;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.time.ZoneOffset;

public class ComponentStorage {

    /**
     * The data source connected to the database which is used by the application.
     */
    private final DataSource dataSource;

    /**
     * The JDBI instance linked to the {@linkplain #dataSource database}.
     */
    private final Jdbi jdbi;

    public static ComponentStorage connectSQLite(final String url, final String... locations) {
        checkArgument(url.startsWith("jdbc:sqlite:"), "SQLite DB URL does not start with 'jdbc:sqlite:': %s", url);

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setDatabaseName("componentstorage");

        return new ComponentStorage(dataSource, locations);
    }

    public ComponentStorage(final DataSource dataSource, final String... locations) {
        this.dataSource = dataSource;
        this.jdbi = Jdbi.create(dataSource);

        // Install the SQL Objects and Guava plugins
        jdbi.installPlugin(new SqlObjectPlugin());
        // Set default timezone to UTC
        jdbi.getConfig(TimestampedConfig.class).setTimezone(ZoneOffset.UTC);

        // Perform Flyway migration on the database
        final Flyway flyway = Flyway.configure()
            .locations(locations)
            .dataSource(dataSource)
            .load();
        flyway.migrate();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
