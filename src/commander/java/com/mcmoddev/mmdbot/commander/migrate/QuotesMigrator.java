/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.migrate;

import static com.mcmoddev.mmdbot.core.util.Constants.Gsons.NO_PRETTY_PRINTING;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.core.util.WithVersionJsonDatabase;
import com.mcmoddev.mmdbot.commander.quotes.NullQuote;
import com.mcmoddev.mmdbot.commander.quotes.StringQuote;
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

@Slf4j
public record QuotesMigrator(Path runPath) {

    private static final Map<String, String> CLASS_CONVERSIONS = Map.of(
        "com.mcmoddev.mmdbot.utilities.quotes.StringQuote", StringQuote.class.getTypeName(),
        "com.mcmoddev.mmdbot.utilities.quotes.NullQuote", NullQuote.class.getTypeName()
    );
    private static final UnaryOperator<Path> QUOTES_FILE_PATH = p -> p.resolve("quotes.json");
    /**
     * The extension appended to JSON data files once their data has been migrated.
     */
    private static final String MIGRATED_FILE_EXTENSION = ".old";

    public void migrate() {
        try {
            final var filePath = QUOTES_FILE_PATH.apply(runPath);
            // Tries migration of an old tricks file, from after the split
            if (!Files.exists(filePath)) {
                final var parent = runPath.toAbsolutePath().getParent();
                if (parent != null) {
                    // Check the ../mmdbot/quotes.json file for after the split
                    final var oldPath = QUOTES_FILE_PATH.apply(parent.resolve("mmdbot"));
                    if (Files.exists(oldPath)) {
                        log.warn("Found old tricks file in {}. Copying...", oldPath);
                        Files.copy(oldPath, filePath);
                        renameMigratedFile(oldPath);
                        migrateUnversioned(filePath);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Exception while trying to migrate old quotes file: ", e);
        }
    }

    /**
     * Tries to migrate an unversioned quotes file
     *
     * @param path the path of the file to migrate
     */
    private void migrateUnversioned(final Path path) throws IOException {
        log.warn("Found old unversioned quotes file... migration started.");
        final var type = new TypeToken<List<JsonObject>>() {}.getType();
        final List<JsonObject> newData = new ArrayList<>();
        try (final var is = new FileReader(path.toFile())) {
            final List<JsonObject> oldData = NO_PRETTY_PRINTING.fromJson(is, type);
            oldData.forEach(oldJson -> {
                final var newType = CLASS_CONVERSIONS.get(oldJson.get("type").getAsString());
                final var newJson = new JsonObject();
                newJson.addProperty("type", newType);
                newJson.add("value", oldJson.get("value"));
                newData.add(newJson);
            });
        } finally {
            try (final var writer = new FileWriter(path.toFile())) {
                final var db = WithVersionJsonDatabase.inMemory(newData, Tricks.CURRENT_SCHEMA_VERSION);
                NO_PRETTY_PRINTING.toJson(db.toJson(NO_PRETTY_PRINTING), writer);
            } finally {
                log.warn("Finished migrating old quotes file.");
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
