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

import com.google.common.primitives.Longs;
import com.mcmoddev.mmdbot.painter.ThePainter;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import com.mcmoddev.mmdbot.painter.util.dao.AutoIconDAO;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class AutoIconsMigration {
    public static void migrate(final Path runPath) throws IOException {
        final List<AutoIconDAO.WithGuildConfiguration> configurations = list(runPath.resolve("autoicons"));
        if (!configurations.isEmpty()) {
            ThePainter.LOGGER.info("Started migration of {} auto icons...", configurations.size());
            final Jdbi db = ThePainter.createDatabaseConnection(runPath);
            db.useExtension(AutoIconDAO.class, dao -> configurations.forEach(conf -> migrate(dao, conf)));
            ThePainter.LOGGER.info("Auto have been icons successfully migrated!");

            for (final AutoIconDAO.WithGuildConfiguration conf : configurations) {
                Files.deleteIfExists(runPath.resolve("autoicons/" + conf.guildId() + ".json"));
            }
        }
    }

    private static void migrate(AutoIconDAO dao, AutoIconDAO.WithGuildConfiguration configuration) {
        dao.set(UserSnowflake.fromId(configuration.guildId()), configuration.configuration());
    }

    @SuppressWarnings("UnstableApiUsage")
    private static List<AutoIconDAO.WithGuildConfiguration> list(final Path path) throws IOException {
        try (final Stream<Path> paths = Files.exists(path) ? Files.list(path) : Stream.empty()) {
            return paths.filter(it -> !Files.isDirectory(it) && Longs.tryParse(nameWithoutExtension(it.getFileName().toString())) != null)
                .map(AutoIconsMigration::from).toList();
        }
    }

    @SneakyThrows
    private static AutoIconDAO.WithGuildConfiguration from(Path path) {
        try (final Reader reader = Files.newBufferedReader(path)) {
            return new AutoIconDAO.WithGuildConfiguration(
                Long.parseLong(nameWithoutExtension(path.getFileName().toString())),
                AutomaticIconConfiguration.GSON.fromJson(reader, AutomaticIconConfiguration.class)
            );
        }
    }

    private static String nameWithoutExtension(final String name) {
        return name.substring(0, name.indexOf('.'));
    }
}
