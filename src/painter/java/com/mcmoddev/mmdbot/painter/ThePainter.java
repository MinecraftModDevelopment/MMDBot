/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.painter;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.database.jdbi.JdbiFactories;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.event.OneTimeEventListener;
import com.mcmoddev.mmdbot.painter.command.RescaleCommand;
import com.mcmoddev.mmdbot.painter.command.TintCommand;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconChanger;
import com.mcmoddev.mmdbot.painter.servericon.auto.command.AutoIconSetCommand;
import com.mcmoddev.mmdbot.painter.util.dao.AutoIconDAO;
import com.mcmoddev.mmdbot.painter.util.dao.DayCounterDAO;
import com.mcmoddev.mmdbot.painter.util.migration.AutoIconsMigration;
import com.mcmoddev.mmdbot.painter.util.migration.DayCounterMigration;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ThePainter implements Bot {
    public static final Logger LOGGER = LoggerFactory.getLogger("ThePainter");

    @RegisterBotType(name = BotRegistry.THE_PAINTER_NAME)
    public static final BotType<ThePainter> BOT_TYPE = new BotType<>() {
        @Override
        public ThePainter createBot(final Path runPath) {
            try {
                return new ThePainter(runPath, DotenvLoader.builder()
                    .filePath(runPath.toAbsolutePath().resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")
                    )
                    .load());
            } catch (IOException e) {
                LOGGER.error("Could not load the .env file due to an IOException: ", e);
            }
            return null;
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }
    };

    private static final OneTimeEventListener<TaskScheduler.CollectTasksEvent> COLLECT_TASKS_LISTENER
        = new OneTimeEventListener<>(event -> event.addTask(new AutomaticIconChanger(), measureNext12PMUTC(), 1, TimeUnit.DAYS));

    private static ThePainter instance;
    public static ThePainter getInstance() {
        return instance;
    }

    private final Dotenv dotenv;
    private final Path runPath;

    private JDA jda;

    private Jdbi jdbi;
    private DayCounterDAO dayCounter;
    private AutoIconDAO autoIcons;

    public ThePainter(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;

        this.jdbi = createDatabaseConnection(runPath);
        this.dayCounter = jdbi.onDemand(DayCounterDAO.class);
        this.autoIcons = jdbi.onDemand(AutoIconDAO.class);

        final var commandClient = new CommandClientBuilder()
            .setOwnerId("000000000000")
            .useHelpBuilder(false)
            .setActivity(null)
            .addSlashCommands(new ServerIconCommand(), new RescaleCommand(), new TintCommand())
            .build();

        COLLECT_TASKS_LISTENER.register(Events.MISC_BUS);

        try {
            final var builder = JDABuilder
                .createLight(getToken(), Set.of())
                .addEventListeners(commandClient, AutoIconSetCommand.INSTANCE);
            jda = builder.build().awaitReady();
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    @Override
    public void migrateData() throws IOException {
        AutoIconsMigration.migrate(runPath);
        DayCounterMigration.migrate(runPath);
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    @Override
    public String getToken() {
        return dotenv.get("BOT_TOKEN");
    }

    public JDA getJDA() {
        return this.jda;
    }

    public Path getRunPath() {
        return runPath;
    }

    public Jdbi database() {
        return jdbi;
    }
    public DayCounterDAO dayCounter() {
        return dayCounter;
    }
    public AutoIconDAO autoIcon() {
        return autoIcons;
    }

    @Override
    public void shutdown() {
        instance = null;
        jda.shutdownNow();
        jda = null;
        jdbi = null;
        dayCounter = null;
    }

    @SuppressWarnings({"MagicConstant"})
    private static Date measureNext12PMUTC() {
        final var day = OffsetDateTime.now(ZoneId.of("UTC")).plusDays(1);
        final var calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.set(day.getYear(), day.getMonthValue() - 1, day.getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }

    public static Jdbi createDatabaseConnection(final Path runPath) {
        final var dbPath = runPath.resolve("data.db");
        if (!Files.exists(dbPath)) {
            try {
                Files.createFile(dbPath);
            } catch (IOException e) {
                throw new RuntimeException("Exception creating database!", e);
            }
        }
        final var url = "jdbc:sqlite:" + dbPath;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setDatabaseName(BotRegistry.THE_PAINTER_NAME);

        final var flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:painter/db")
            .load();
        flyway.migrate();

        return Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin())
            .registerArgument(JdbiFactories.JdbiArgumentFactory.FACTORY);
    }
}
