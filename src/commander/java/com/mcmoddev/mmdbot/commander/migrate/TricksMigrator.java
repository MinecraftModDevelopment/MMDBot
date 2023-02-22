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
package com.mcmoddev.mmdbot.commander.migrate;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.commander.tricks.EmbedTrick;
import com.mcmoddev.mmdbot.commander.tricks.ScriptTrick;
import com.mcmoddev.mmdbot.commander.tricks.StringTrick;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.core.database.VersionedDatabase;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.mcmoddev.mmdbot.core.util.Constants.Gsons.NO_PRETTY_PRINTING;

@Slf4j
public record TricksMigrator(Path runPath) {

    private static final Map<String, String> CLASS_CONVERSIONS = Map.of(
        "com.mcmoddev.mmdbot.utilities.tricks.EmbedTrick", EmbedTrick.class.getTypeName(),
        "com.mcmoddev.mmdbot.utilities.tricks.ScriptTrick", ScriptTrick.class.getTypeName(),
        "com.mcmoddev.mmdbot.utilities.tricks.StringTrick", StringTrick.class.getTypeName()
    );
    private static final UnaryOperator<Path> OLD_TRICKS_PATH = p -> p.resolve("mmdbot_tricks.json");
    private static final UnaryOperator<Path> NEW_TRICKS_PATH = p -> p.resolve("tricks.json");
    /**
     * The extension appended to JSON data files once their data has been migrated.
     */
    private static final String MIGRATED_FILE_EXTENSION = ".old";

    public void migrate() {
        try {
            migrateOldFiles();
        } catch (IOException e) {
            log.error("Exception while trying to migrate old tricks file: ", e);
        }
    }

    /**
     * Tries migrating old files.
     */
    private void migrateOldFiles() throws IOException {
        var oldFilePath = OLD_TRICKS_PATH.apply(runPath);
        if (!Files.exists(oldFilePath)) {
            final var parent = runPath.toAbsolutePath().getParent();
            if (parent != null) {
                // Check the ../mmdbot/mmdbot_tricks.json file for after the split
                oldFilePath = OLD_TRICKS_PATH.apply(parent.resolve("mmdbot"));
                if (!Files.exists(oldFilePath)) {
                    return;
                }
            } else {
                return;
            }
        }
        log.warn("Found old tricks file... migration started.");
        final var type = new TypeToken<java.util.List<JsonObject>>() {
        }.getType();
        final List<JsonObject> newData = new ArrayList<>();
        try (final var is = new FileReader(oldFilePath.toFile())) {
            final List<JsonObject> oldData = NO_PRETTY_PRINTING.fromJson(is, type);
            oldData.forEach(oldJson -> {
                final var newType = CLASS_CONVERSIONS.get(oldJson.get("$type").getAsString());
                final var newJson = new JsonObject();
                newJson.addProperty("$type", newType);
                newJson.add("value", oldJson.get("value"));
                newData.add(newJson);
            });
        } finally {
            final var newTricksPath = NEW_TRICKS_PATH.apply(runPath);
            if (!Files.exists(newTricksPath)) {
                Files.createFile(newTricksPath);
            }
            try (final var writer = new FileWriter(newTricksPath.toFile())) {
                final var db = VersionedDatabase.inMemory(Tricks.CURRENT_SCHEMA_VERSION, newData);
                NO_PRETTY_PRINTING.toJson(db.toJson(NO_PRETTY_PRINTING), writer);
            } finally {
                renameMigratedFile(oldFilePath);
                log.warn("Finished migrating old tricks file.");
            }
        }
    }

    /**
     * Renames the file to append the {@link #MIGRATED_FILE_EXTENSION} ({@value #MIGRATED_FILE_EXTENSION}), and logs
     * any exceptions silently.
     *
     * @param file the file to rename
     */
    private static void renameMigratedFile(final Path file) {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + MIGRATED_FILE_EXTENSION),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            log.warn("Failed to rename migrated data file {}", file, exception);
        }
    }
}
