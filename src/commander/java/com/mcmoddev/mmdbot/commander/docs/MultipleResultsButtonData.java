package com.mcmoddev.mmdbot.commander.docs;

import de.ialistannen.javadocapi.querying.FuzzyQueryResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record MultipleResultsButtonData(long userId, List<String> queries) {

    public MultipleResultsButtonData(long userId, Collection<FuzzyQueryResult> results) {
        this(userId, results.stream().map(r -> r.getQualifiedName().asString()).collect(Collectors.toList()));
    }

    public static MultipleResultsButtonData fromArguments(final List<String> args) {
        return new MultipleResultsButtonData(Long.parseLong(args.get(0)), args.subList(1, args.size()));
    }

    public List<String> toArguments() {
        if (queries instanceof ArrayList<String>) {
            queries.add(0, String.valueOf(userId));
            return queries;
        }
        final var copy = new ArrayList<>(queries);
        copy.add(0, String.valueOf(userId));
        return copy;
    }

}
