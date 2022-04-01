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
package com.mcmoddev.updatinglauncher;

import com.mcmoddev.updatinglauncher.github.Release;
import com.mcmoddev.updatinglauncher.github.UpdateChecker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JarUpdater implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger("JarUpdater");

    private final Path jarPath;
    private final UpdateChecker updateChecker;
    private final Pattern jarNamePattern;
    private final List<String> javaArgs;

    private Process process;

    public JarUpdater(@NonNull final Path jarPath, @NonNull final UpdateChecker updateChecker, @NonNull final Pattern jarNamePattern, @NonNull final List<String> javaArgs) {
        this.jarPath = jarPath;
        this.updateChecker = updateChecker;
        this.jarNamePattern = jarNamePattern;
        this.javaArgs = javaArgs;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process != null) {
                process.destroyForcibly();
            }
        }));
    }

    @Override
    public void run() {
        LOGGER.info("Started update checking...");
        try {
            if (updateChecker.findNew()) {
                final var release = updateChecker.getLatestFound();
                if (release == null) return;
                final var as = release.assets.stream()
                    .filter(asst -> asst.name.endsWith(".jar"))
                    .filter(p -> jarNamePattern.matcher(p.name).find())
                    .findFirst();
                if (as.isPresent()) {
                    LOGGER.warn("Found new update to version \"{}\"!", release.name);
                    if (process != null) {
                        process.onExit().whenComplete(($, $$) -> {
                            if ($$ != null) {
                                LOGGER.error("Exception trying to destroy old process: ", $$);
                                return;
                            }
                            try {
                                update(as.get());
                            } catch (Exception e) {
                                LOGGER.warn("Exception trying to update jar: ",e);
                            }
                            process = createProcess();
                            LOGGER.warn("Old process was destroyed!");
                        });
                        process.destroy();
                    } else {
                        update(as.get());
                        process = createProcess();
                    }
                }
            } else {
                LOGGER.info("No updates were found.");
            }
        } catch (Exception e) {
            LOGGER.error("Exception trying to update jar: ", e);
        }
    }

    private void update(final Release.Asset asset) throws Exception {
        final var parent = jarPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.deleteIfExists(jarPath);
        try (final var is = new URL(asset.browserDownloadUrl).openStream()) {
            Files.copy(is, jarPath);
        }
    }

    private Process createProcess() {
        try {
            LOGGER.info("Starting process...");
            return new ProcessBuilder(getStartCommand()).inheritIO().start();
        } catch(IOException e) {
            LOGGER.error("Starting process failed, used start command {}", getStartCommand(), e);
        }
        return null;
    }

    private List<String> getStartCommand() {
        List<String> command = new ArrayList<>(javaArgs.size() + 2);
        command.add("java");
        command.addAll(javaArgs);
        command.add("-jar");
        command.add(jarPath.toString());
        return command;
    }
}
