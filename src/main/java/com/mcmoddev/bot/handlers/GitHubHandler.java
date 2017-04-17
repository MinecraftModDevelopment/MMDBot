package com.mcmoddev.bot.handlers;

import java.io.IOException;

import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

import com.mcmoddev.bot.util.Tuple;

public class GitHubHandler {

    public static GitHub github;

    public static void init (String oauth) {

        try {

            github = GitHub.connect("MinecraftModDevelopmentBot", oauth);

            System.out.println(github.getApiUrl());
        }

        catch (final IOException e) {

            e.printStackTrace();
        }
    }

    public static GHGist createGist (boolean isPublic, String description, String name, String content) {

        return createGist(isPublic, description, new Tuple<>(name, content));
    }

    public static GHGist createGist (boolean isPublic, String description, Tuple<String, String>... content) {

        final GHGistBuilder builder = github.createGist();
        builder.public_(isPublic);
        builder.description(description);

        for (final Tuple<String, String> file : content)
            builder.file(file.x, file.y);

        try {

            return builder.create();
        }

        catch (final IOException e) {

            e.printStackTrace();
        }

        return null;
    }
}
