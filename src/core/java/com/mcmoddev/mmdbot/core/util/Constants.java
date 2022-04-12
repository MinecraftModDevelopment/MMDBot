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

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmoddev.mmdbot.core.util.gson.InstantTypeAdapter;
import com.mcmoddev.mmdbot.core.util.gson.PatternTypeAdapter;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import lombok.experimental.UtilityClass;
import org.spongepowered.configurate.reference.WatchServiceListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Instant;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A class holding common constants
 */
@UtilityClass
public final class Constants {

    public static final Joiner LINE_JOINER = Joiner.on(System.lineSeparator());

    public static final WatchServiceListener CONFIG_WATCH_SERVICE = io.github.matyrobbrt.curseforgeapi.util.Utils.rethrowSupplier(() -> WatchServiceListener
        .builder()
        .threadFactory(r -> Utils.setThreadDaemon(new Thread(r, "ConfigListener"), true))
        .build()).get();

    /**
     * The constant random.
     */
    public static final Random RANDOM = new Random();

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * The URI of the maven central repo.
     */
    public static final URI MAVEN_CENTRAL = URI.create("https://repo1.maven.org/maven2/");

    /**
     * A pattern that matches a maven artifact location, like com.example:example
     * <h4>Groups</h4>
     * <table>
     *     <tr>
     *         <th>Index</th>
     *         <th>Name</th>
     *         <th>Description</th>
     *     </tr>
     *     <tr>
     *         <td>0</td>
     *         <td>N/A</td>
     *         <td>The entire location</td>
     *     </tr>
     *     <tr>
     *         <td>1</td>
     *         <td>group</td>
     *         <td>The group of the artifact</td>
     *     </tr>
     *     <tr>
     *         <td>2</td>
     *         <td>id</td>
     *         <td>The ID of the artifact</td>
     *     </tr>
     * </table>
     */
    public static final Pattern ARTIFACT_LOCATION_PATTERN = Pattern.compile("(?<group>.*):(?<id>.*)");

    public static final class Gsons {
        public static final Gson NO_PRETTY_PRINTING = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .disableHtmlEscaping()
            .setLenient()
            .create();
        public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }
}
