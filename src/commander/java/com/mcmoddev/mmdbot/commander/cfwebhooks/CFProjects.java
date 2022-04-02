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
package com.mcmoddev.mmdbot.commander.cfwebhooks;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.mcmoddev.mmdbot.core.util.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class CFProjects implements Runnable {
    private final Path filePath;
    private List<CFProject> projects;

    public CFProjects(final Path filePath) {
        this.filePath = filePath;
        load();
    }

    public List<CFProject> getProjects() {
        if (projects == null) {
            load();
        }
        return projects;
    }

    public Optional<CFProject> getProjectById(int projectId) {
        return getProjects()
            .stream()
            .filter(p -> p.projectId() == projectId)
            .findAny();
    }

    public void addProject(int projectId, long channelId) {
        getProjectById(projectId).ifPresentOrElse(cfProject -> cfProject.channels().add(channelId),
            () -> projects.add(new CFProject(projectId, Sets.newHashSet(channelId), new AtomicInteger(0))));
        save();
    }

    public void removeProject(int projectId, long channelId) {
        getProjectById(projectId).ifPresent(project -> {
            project.channels().remove(channelId);
            if (project.channels().isEmpty()) {
                getProjects().remove(project);
            }
            save();
        });
    }

    public List<Integer> getProjectsForChannel(long channelId) {
        return getProjects().stream()
            .filter(p -> p.channels().contains(channelId))
            .map(CFProject::projectId)
            .toList();
    }

    public void load() {
        try {
            createFileIfNotExists();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                Type typeOfList = new TypeToken<List<CFProject>>() {
                }.getType();
                projects = Constants.Gsons.NO_PRETTY_PRINTING.fromJson(reader, typeOfList);
            }
        } catch (IOException e) {
            log.error("Exception while reading CurseForgeProjects file", e);
        }
    }

    public void save() {
        try {
            createFileIfNotExists();
            final var projects$ = getProjects();
            try (var writer = new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
                Constants.Gsons.NO_PRETTY_PRINTING.toJson(projects$, writer);
            }
        } catch (IOException e) {
            log.error("Exception while saving CurseForgeProjects file", e);
        }
    }

    private void createFileIfNotExists() throws IOException {
        if (!Files.exists(filePath)) {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.createFile(filePath);
            try (final var fw = new FileWriter(filePath.toFile())) {
                Constants.Gsons.NO_PRETTY_PRINTING.toJson(new JsonArray(), new JsonWriter(fw));
            }
        }
    }

    @Override
    public void run() {
        getProjects().forEach(CFProject::run);
    }
}
