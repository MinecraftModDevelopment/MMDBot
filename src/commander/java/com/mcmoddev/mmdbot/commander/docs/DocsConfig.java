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
        @Comment("An URL or a path relative to '/docs' which ")
        private String indexUrl = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0-sources.jar";
    }

}
