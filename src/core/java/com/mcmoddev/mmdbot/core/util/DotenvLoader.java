/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.core.util;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DotenvLoader {
    public static DotenvLoader builder() {
        return new DotenvLoader();
    }

    private Path filePath = Path.of(".env");
    private boolean systemProperties = false;
    private boolean throwIfMalformed = true;
    private Consumer<DotenvWriter> whenCreated;

    /**
     * Sets the consumer which will be run when a .env file is created, if it doesn't exist
     *
     * @param whenCreated the consumer
     * @return this {@link DotenvLoader}
     */
    public DotenvLoader whenCreated(Consumer<DotenvWriter> whenCreated) {
        this.whenCreated = whenCreated;
        return this;
    }

    /**
     * Sets the path of the .env
     *
     * @param filePath the path of the .env file
     * @return this {@link DotenvLoader}
     */
    public DotenvLoader filePath(Path filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * Does not throw an exception when .env is malformed.
     *
     * @return this {@link DotenvLoader}
     */
    public DotenvLoader ignoreIfMalformed() {
        throwIfMalformed = false;
        return this;
    }

    /**
     * Sets each environment variable as system properties.
     *
     * @return this {@link DotenvLoader}
     */
    public DotenvLoader systemProperties() {
        systemProperties = true;
        return this;
    }

    /**
     * Load the contents of .env into the virtual environment.
     *
     * @return a new {@link Dotenv} instance
     * @throws DotenvException when an error occurs
     */
    public Dotenv load() throws DotenvException, IOException {
        if (!Files.exists(filePath)) {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.createFile(filePath);

            if (whenCreated != null) {
                try (final var pWriter = new PrintWriter(new FileWriter(filePath.toFile()))) {
                    whenCreated.accept(new DotenvWriter() {
                        @Override
                        public DotenvWriter writeValue(@NonNull final String key, @Nullable final String value) {
                            pWriter.println("%s=%s".formatted(key, value == null ? "" : value));
                            return this;
                        }

                        @Override
                        public DotenvWriter writeComment(@NonNull final String comment) {
                            pWriter.write("# ");
                            pWriter.println(comment);
                            return this;
                        }
                    });
                }
            }
        }

        final var builder = new DotenvBuilder()
            .directory(filePath.getParent() == null ? "" : filePath.getParent().toString())
            .filename(filePath.getFileName().toString());
        if (!throwIfMalformed) {
            builder.ignoreIfMalformed();
        }
        if (systemProperties) {
            builder.systemProperties();
        }
        return builder.load();
    }

    public interface DotenvWriter {
        DotenvWriter writeValue(@NonNull String key, @Nullable String value);

        DotenvWriter writeComment(@NonNull String comment);
    }
}
