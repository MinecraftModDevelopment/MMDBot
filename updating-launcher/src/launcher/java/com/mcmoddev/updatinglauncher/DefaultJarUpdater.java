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

import com.mcmoddev.updatinglauncher.api.ProcessInfo;
import com.mcmoddev.updatinglauncher.api.Properties;
import com.mcmoddev.updatinglauncher.api.Release;
import com.mcmoddev.updatinglauncher.api.UpdateChecker;
import com.mcmoddev.updatinglauncher.api.connector.ProcessConnector;
import com.mcmoddev.updatinglauncher.discord.DiscordIntegration;
import net.dv8tion.jda.api.entities.Activity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public class DefaultJarUpdater implements com.mcmoddev.updatinglauncher.api.JarUpdater {
    public static final Logger LOGGER = LoggerFactory.getLogger("JarUpdater");

    private final Path jarPath;
    private final UpdateChecker updateChecker;
    private final List<String> javaArgs;
    private final Map<String, String> properties;
    private final LoggingWebhook loggingWebhook;
    private final DiscordIntegration integration;

    @Nullable
    private ProcessInfo process;

    public DefaultJarUpdater(@NonNull final Path jarPath, @NonNull final UpdateChecker updateChecker, @NonNull final List<String> javaArgs, String webhookUrl, final DiscordIntegration integration) {
        this.jarPath = jarPath.toAbsolutePath();
        this.updateChecker = updateChecker;
        this.javaArgs = javaArgs;
        this.integration = integration;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process != null) {
                process.process().destroy();
            }
        }));

        properties = Map.of(
            Properties.JAR_PATH, jarPath.toString()
        );

        if (!webhookUrl.isBlank()) {
            // maybe use a regex?
            webhookUrl = webhookUrl.replace("https://discord.com/api/webhooks/", "");
            loggingWebhook = new LoggingWebhook(webhookUrl.substring(0, webhookUrl.indexOf('/')), webhookUrl.substring(webhookUrl.indexOf('/')));
        } else {
            loggingWebhook = null;
        }
    }

    @Override
    public void run() {
        LOGGER.info("Started update checking...");
        try {
            if (updateChecker.findNew()) {
                final var release = updateChecker.getLatestFound();
                if (release != null) {
                    LOGGER.warn("Found new update to version \"{}\"!", release.name());
                    killAndUpdate(release);
                }
            } else {
                LOGGER.info("No updates were found.");
            }
        } catch (Exception e) {
            LOGGER.error("Exception trying to update jar: ", e);
        }
    }

    @Override
    public void killAndUpdate(final Release release) throws Exception {
        if (process != null) {
            process.process().onExit().whenComplete(($, $$) -> {
                if ($$ != null) {
                    return;
                }
                try {
                    update(release);
                } catch (Exception e) {
                    LOGGER.warn("Exception trying to update jar: ",e);
                }
                process = new ProcessInfoImpl(createProcess(), release);
                LOGGER.warn("Old process was destroyed!");
            });
            process.process().destroy();
        } else {
            update(release);
            process = new ProcessInfoImpl(createProcess(), release);
        }
    }

    public void update(final Release release) throws Exception {
        if (integration != null) {
            integration.getJda().getPresence().setActivity(Activity.of(Activity.ActivityType.CUSTOM_STATUS, "Updating a process \uD83D\uDD04"));
        }

        final var parent = jarPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.deleteIfExists(jarPath);
        try (final var is = new BufferedInputStream(new URL(release.url()).openStream())) {
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
            if (integration != null) {
                integration.setActivity(true);
            }
            return new ProcessBuilder(getStartCommand())
                .inheritIO()
                .start();
        } catch (IOException e) {
            LOGGER.error("Starting process failed, used start command {}", getStartCommand(), e);
        }
        if (integration != null) {
            integration.setActivity(false); // exception in this case
        }
        return null;
    }

    @Override
    public void tryFirstStart() {
        if (Files.exists(jarPath)) {
            process = new ProcessInfoImpl(Objects.requireNonNull(createProcess()), null);
            LOGGER.warn("Started process after launcher start.");
        }
    }

    private List<String> getStartCommand() {
        List<String> command = new ArrayList<>(javaArgs.size() + 2);
        command.add(findJavaBinary());
        final var webhookUrl = loggingWebhook == null ? "" : "/;/" + loggingWebhook.id() + "%%" + loggingWebhook.token();
        command.add("-javaagent:" + Main.AGENT_PATH.toAbsolutePath() + "=" + Main.RMI_NAME + webhookUrl);
        command.addAll(javaArgs);
        properties.forEach((key, value) -> command.add("-D%s=\"%s\"".formatted(key, value)));
        command.add("-jar");
        command.add(jarPath.toString());
        return command;
    }

    private static String findJavaBinary() {
        return ProcessHandle.current().info().command().orElse("java");
    }

    @Override
    public void startProcess() {
        process = new ProcessInfoImpl(createProcess(), updateChecker.getLatestFound());
    }

    @Override
    public void clearProcess() {
        process = null;
    }

    @Override
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    @Override
    public Path getJarPath() {
        return jarPath;
    }

    @Override
    public Optional<String> getJarVersion() {
        if (!Files.exists(jarPath)) {
            if (process == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(process.release()).map(Release::name);
        } else {
            try {
                final var jFile = new JarFile(jarPath.toFile());
                return Optional.ofNullable(jFile.getManifest()).flatMap(m -> Optional.ofNullable(m.getMainAttributes().getValue("Implementation-Version")));
            } catch (IOException e) {
                return Optional.empty();
            }
        }
    }

    @Override
    @Nullable
    public ProcessInfo getProcess() {
        return process;
    }

    private class ProcessInfoImpl implements ProcessInfo {
        private final Process process;
        @Nullable
        private final Release release;
        private ProcessConnector connector;

        public ProcessInfoImpl(final Process process, @Nullable final Release release) {
            this.process = new DelegatedProcess(process) {
                @Override
                public void destroy() {
                    if (integration != null) {
                        integration.setActivity(false);
                    }
                    if (connector != null) {
                        try {
                            connector.onShutdown();
                        } catch (RemoteException e) {
                            LOGGER.error("Exception trying to call shutdown listeners: ", e);
                        }
                    }
                    super.destroy();
                }

                @Override
                public Process destroyForcibly() {
                    if (integration != null) {
                        integration.setActivity(false);
                    }
                    return super.destroyForcibly();
                }
            };
            this.release = release;
            process.onExit().whenComplete(($, e) -> {
               if (e != null) {
                   DefaultJarUpdater.LOGGER.error("Exception exiting process: ", e);
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
            }, 20, TimeUnit.SECONDS);
        }

        @Override
        public Process process() {
            return process;
        }

        @Override
        @Nullable
        public Release release() {
            return release;
        }

        @Override
        @Nullable
        public ProcessConnector connector() {
            return connector;
        }
    }

    record LoggingWebhook(String id, String token) {}

}
