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
package com.mcmoddev.mmdbot.commander.docs;

import de.ialistannen.javadocapi.spoon.JavadocLauncher;
import de.ialistannen.javadocapi.storage.ConfiguredGson;
import de.ialistannen.javadocapi.storage.SqliteStorage;
import spoon.support.compiler.ZipFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ManualIndexer {
    public static void main(String[] args) throws Exception {
        final var inPath = Path.of("run/thecommander/docs/input/groovy-4.0.4-sources.jar");
        final var outPath = Path.of("run/thecommander/docs/groovy_docs.db");
        Files.createFile(outPath);

        final var launcher = new JavadocLauncher();
        launcher.addInputResource(new ZipFolder(inPath.toFile()));

        final var inLibs = new ArrayList<URL>();

        inLibs.add(get("\"C:\\Users\\rober\\.gradle\\caches\\modules-2\\files-2.1\\org.codehaus.gpars\\gpars\\1.2.1\\c3ea0fbcd67a163bd5e3a3efdaa3428262d0d437\\gpars-1.2.1.jar\""));

        launcher.getEnvironment().setInputClassLoader(new URLClassLoader(inLibs.toArray(URL[]::new)));

        ConfigBasedElementLoader.index(launcher, new SqliteStorage(
            ConfiguredGson.create(), outPath
        ));
    }

    private static URL get(@SuppressWarnings("SameParameterValue") String path) throws IOException {
        return new File(path.replace("\"", "")).toURI().toURL();
    }
}
