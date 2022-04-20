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

import com.mcmoddev.mmdbot.core.util.config.ConfigurateUtils;
import com.mcmoddev.mmdbot.core.util.Constants;
import de.ialistannen.javadocapi.indexing.OnlineJavadocIndexer;
import de.ialistannen.javadocapi.rendering.Java11PlusLinkResolver;
import de.ialistannen.javadocapi.spoon.JavadocElementExtractor;
import de.ialistannen.javadocapi.spoon.filtering.IndexerFilterChain;
import de.ialistannen.javadocapi.spoon.filtering.ParallelProcessor;
import de.ialistannen.javadocapi.storage.AggregatedElementLoader;
import de.ialistannen.javadocapi.storage.ConfiguredGson;
import de.ialistannen.javadocapi.storage.ElementLoader;
import de.ialistannen.javadocapi.storage.SqliteStorage;
import de.ialistannen.javadocapi.util.BaseUrlElementLoader;
import de.ialistannen.javadocapi.util.ExternalJavadocReference;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import spoon.Launcher;
import spoon.OutputType;
import spoon.support.compiler.ZipFolder;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class ConfigBasedElementLoader implements ElementLoader {

    private static final Executor INDEX_EXECUTOR;

    static {
        final var group = new ThreadGroup("JavaDocIndexer");
        final var ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(5, r -> new Thread(group, r, "JavaDocIndexer #" + group.activeCount()));
        ex.setKeepAliveTime(5, TimeUnit.MINUTES);
        ex.allowCoreThreadTimeOut(true);
        INDEX_EXECUTOR = ex;
    }

    private final Path basePath;
    @Delegate
    private final ElementLoader delegate;

    private final int loadersSize;
    private int indexedAmount;

    public ConfigBasedElementLoader(final Path basePath) throws Exception {
        this.basePath = basePath;
        final var cfgPath = basePath.resolve("config.conf");
        if (!Files.exists(basePath)) Files.createDirectories(basePath);
        final var loader = HoconConfigurationLoader.builder()
            .emitComments(true)
            .prettyPrinting(true)
            .path(cfgPath)
            .build();
        final var config = ConfigurateUtils.loadConfig(loader, cfgPath, e -> {
        }, DocsConfig.class, DocsConfig.DEFAULT).first().get();
        Objects.requireNonNull(config);

        final var gson = ConfiguredGson.create();
        final var loaders = new ArrayList<ElementLoader>();
        for (final var database : config.getDatabases()) {
            final var dbPath = resolve(basePath, database.getPath());
            final var storage = new SqliteStorage(
                gson, dbPath
            );
            if (!Files.exists(dbPath)) {
                INDEX_EXECUTOR.execute(() -> {
                    try {
                        tryIndex(database.getIndexUrl(), storage);
                        indexedAmount++;
                    } catch (Exception e) {
                        log.error("There was an exception trying to index JavaDocs: ", e);
                        indexedAmount++;
                    }
                });
            } else {
                indexedAmount++;
            }
            loaders.add(new BaseUrlElementLoader(
                storage, database.getBaseUrl(), indexExternalJavadoc(database.getExternalJavadocs()), new Java11PlusLinkResolver()
            ));
        }
        loadersSize = loaders.size();
        this.delegate = new AggregatedElementLoader(loaders);
    }

    public boolean indexedAll() {
        return indexedAmount >= loadersSize;
    }

    private void tryIndex(final String indexUrl, final SqliteStorage storage) throws Exception {
        final var parent = storage.getFile().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.createFile(storage.getFile());
        final var launcher = new Launcher();
        if (indexUrl.startsWith("https://") || indexUrl.startsWith("http://")) {
            log.info("Downloading {} for indexing...", indexUrl);
            try (final var readChannel = Channels.newChannel(new URL(indexUrl).openStream())) {
                final var downloadFile = storage.getFile().getParent().resolve("index_" + storage.getFile().toFile().getName() + (indexUrl.endsWith(".jar") ? ".jar" : ".zip"));
                Files.createFile(downloadFile);
                try (final var writeChannel = new FileOutputStream(downloadFile.toFile()).getChannel()) {
                    writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
                } finally {
                    launcher.addInputResource(new ZipFolder(downloadFile.toFile()));
                }
            }
        } else {
            final var path = resolve(basePath, indexUrl);
            if (indexUrl.endsWith(".zip") || indexUrl.endsWith(".jar")) {
                launcher.addInputResource(new ZipFolder(path.toFile()));
            } else {
                launcher.addInputResource(path.toAbsolutePath().toString());
            }
        }
        index(launcher, storage);
    }

    private void index(final Launcher launcher, final SqliteStorage storage) {
        log.warn("Started indexing JavaDocs...");
        launcher.getEnvironment().setShouldCompile(false);
        launcher.getEnvironment().disableConsistencyChecks();
        launcher.getEnvironment().setOutputType(OutputType.NO_OUTPUT);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.getEnvironment().setComplianceLevel(16);

        final var model = launcher.buildModel();
        final var extractor = new JavadocElementExtractor();
        final var processor = new ParallelProcessor(
            new IndexerFilterChain(Set.of("*")).asFilter(),
            Runtime.getRuntime().availableProcessors()
        );
        model.getAllModules()
            .forEach(it -> processor.process(
                it,
                element -> element.accept(extractor))
            );
        processor.shutdown();
        storage.addAll(extractor.getFoundElements());
        log.warn("Finished indexing JavaDocs.");
    }

    private static Path resolve(final Path basePath, final String other) {
        var path = basePath;
        for (final var p : other.split("/")) {
            path = path.resolve(p);
        }
        return path;
    }

    private static List<ExternalJavadocReference> indexExternalJavadoc(List<String> urls)
        throws Exception {
        if (urls == null) {
            return List.of();
        }

        final var indexer = new OnlineJavadocIndexer(Constants.HTTP_CLIENT);
        final var references = new ArrayList<ExternalJavadocReference>();

        for (final var url : urls) {
            final var reference = indexer.fetchPackages(url);
            references.add(reference);
        }

        return references;
    }
}
