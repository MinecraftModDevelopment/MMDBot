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
package com.mcmoddev.mmdbot.painter.util.migration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.util.dao.DayCounterDAO;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class DayCounterMigration {
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping().create();

    public static void migrate(Path runPath) throws IOException {
        final Path daysPath = runPath.resolve("autoicons/days.json");

        if (Files.exists(daysPath)) {
            ThePainter.LOGGER.info("Migrating day data....");

            try (final Reader reader = Files.newBufferedReader(daysPath)) {
                final Jdbi db = ThePainter.createDatabaseConnection(runPath);
                final Map<String, DayData> days = GSON.fromJson(reader, new TypeToken<>() {});
                DayCounterMigration.migrateDaysToDB(db, days);
            }

            ThePainter.LOGGER.info("Successfully migrated day data.");
            Files.delete(daysPath);
        }
    }

    private static void migrateDaysToDB(Jdbi database, Map<String, DayData> days) {
        database.useExtension(DayCounterDAO.class,
            dayCounter -> days.forEach((guildId, data) ->
                dayCounter.update(UserSnowflake.fromId(guildId), data.day, data.backwards)));
    }

    public record DayData(int day, boolean backwards) {}
}
