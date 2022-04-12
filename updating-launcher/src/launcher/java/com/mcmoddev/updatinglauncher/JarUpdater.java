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
package com.mcmoddev.updatinglauncher;

import com.mcmoddev.updatinglauncher.github.Release;
import com.mcmoddev.updatinglauncher.github.UpdateChecker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class JarUpdater implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger("JarUpdater");

    private final Path jarPath;
    private final UpdateChecker updateChecker;
    private final Pattern jarNamePattern;
    private final List<String> javaArgs;

    @Nullable
    private ProcessInfo process;

    public JarUpdater(@NonNull final Path jarPath, @NonNull final UpdateChecker updateChecker, @NonNull final Pattern jarNamePattern, @NonNull final List<String> javaArgs) {
        this.jarPath = jarPath;
        this.updateChecker = updateChecker;
        this.jarNamePattern = jarNamePattern;
        this.javaArgs = javaArgs;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process != null) {
                process.process().destroy();
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
                    killAndUpdate(release, as.get());
                }
            } else {
                LOGGER.info("No updates were found.");
            }
        } catch (Exception e) {
            LOGGER.error("Exception trying to update jar: ", e);
        }
    }

    public void killAndUpdate(final Release release, final Release.Asset asset) throws Exception {
        if (process != null) {
            process.process().onExit().whenComplete(($, $$) -> {
                if ($$ != null) {
                    return;
                }
                try {
                    update(asset);
                } catch (Exception e) {
                    LOGGER.warn("Exception trying to update jar: ",e);
                }
                process = new ProcessInfo(createProcess(), release);
                LOGGER.warn("Old process was destroyed!");
            });
            process.process().destroy();
        } else {
            update(asset);
            process = new ProcessInfo(createProcess(), release);
        }
    }

    public void update(final Release.Asset asset) throws Exception {
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
            if (!Files.exists(Main.AGENT_PATH)) {
                try {
                    Main.copyAgent();
                } catch (IOException e) {
                    LOGGER.error("Exception copying agent JAR: ", e);
                    throw new RuntimeException(e);
                }
            }

            LOGGER.info("Starting process...");
            return new ProcessBuilder(getStartCommand())
                .inheritIO()
                .start();
        } catch (IOException e) {
            LOGGER.error("Starting process failed, used start command {}", getStartCommand(), e);
        }
        return null;
    }

    public void tryFirstStart() {
        if (Files.exists(jarPath)) {
            process = new ProcessInfo(Objects.requireNonNull(createProcess()), null);
            LOGGER.warn("Starting process after launcher start.");
        }
    }

    private List<String> getStartCommand() {
        List<String> command = new ArrayList<>(javaArgs.size() + 2);
        command.add(findJavaBinary());
        command.add("-javaagent:" + Main.AGENT_PATH.toAbsolutePath());
        command.add("-D" + ProcessConnector.NAME_PROPERTY + "=" + '"' + Main.RMI_NAME + '"');
        command.addAll(javaArgs);
        command.add("-jar");
        command.add(jarPath.toString());
        return command;
    }

    private static String findJavaBinary() {
        return ProcessHandle.current().info().command().orElse("java");
    }

    public void runProcess() {
        process = new ProcessInfo(createProcess(), updateChecker.getLatestFound());
    }

    public void clearProcess() {
        process = null;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public Optional<Release.Asset> resolveAssetFromRelease(final Release release) {
        return release.assets.stream()
            .filter(asst -> asst.name.endsWith(".jar"))
            .filter(p -> jarNamePattern.matcher(p.name).find())
            .findFirst();
    }

    public Path getJarPath() {
        return jarPath;
    }

    public Optional<String> getJarVersion() {
        if (!Files.exists(jarPath)) {
            if (process == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(process.release()).map(r -> r.name);
        } else {
            try {
                final var jFile = new JarFile(jarPath.toFile());
                return Optional.ofNullable(jFile.getManifest()).flatMap(m -> Optional.ofNullable(m.getMainAttributes().getValue("Implementation-Version")));
            } catch (IOException e) {
                return Optional.empty();
            }
        }
    }

    @Nullable
    public ProcessInfo getProcess() {
        return process;
    }

    public static class ProcessInfo {
        private final Process process;
        @Nullable
        private final Release release;
        private ProcessConnector connector;

        public ProcessInfo(final Process process, @Nullable final Release release) {
            this.process = process;
            this.release = release;
            process.onExit().whenComplete(($, e) -> {
               if (e != null) {
                   JarUpdater.LOGGER.error("Exception exiting process: ", e);
               } else {
                   LOGGER.warn("Process exited successfully.");
               }
            });

            Main.SERVICE.schedule(() -> {
                try {
                    final var registry = LocateRegistry.getRegistry("127.0.0.1", ProcessConnector.PORT);
                    connector = (ProcessConnector) registry.lookup(Main.RMI_NAME);
                    LOGGER.warn("RMI connector has been successfully setup at port {}", ProcessConnector.PORT);
                } catch (Exception e) {
                    LOGGER.error("Exception setting up RMI connector: ", e);
                }
            }, 30, TimeUnit.SECONDS);
        }

        public Process process() {
            return process;
        }

        @Nullable
        public Release release() {
            return release;
        }

        @Nullable
        public ProcessConnector connector() {
            return connector;
        }
    }
}
