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
package com.mcmoddev.mmdbot.commander.docs;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class DocsConfig {

    public static final DocsConfig DEFAULT = new DocsConfig();

    @Getter
    @NonNull
    @Required
    @Setting("databases")
    @Comment("A list of databases to use for the docs command.")
    private List<Database> databases = Lists.newArrayList(new Database());

    @ConfigSerializable
    public static final class Database {

        @Getter
        @Required
        @Setting("path")
        @Comment("The path of the database on which to save indexed data. Relative to '/docs'")
        private String path = "gson_docs.db";

        @Getter
        @Required
        @Setting("baseUrl")
        @Comment("An URL for the javadocs to index.")
        private String baseUrl = "https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson";

        @Getter
        @Required
        @Setting("externalJavadocs")
        @Comment("A list of URLs for linking external docs with the main JavaDocs.")
        private List<String> externalJavadocs = new ArrayList<>();

        @Getter
        @Required
        @Setting("indexUrl")
        @Comment("An URL or a path relative to '/docs' from which to index the javadocs, from the sources")
        private String indexUrl = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0-sources.jar";
    }

}
