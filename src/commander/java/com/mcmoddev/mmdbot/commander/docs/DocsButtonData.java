package com.mcmoddev.mmdbot.commander.docs;

import java.util.List;

public record DocsButtonData(long buttonOwner, String query, boolean shortDescription, boolean omitTags) {

    public static DocsButtonData fromArguments(final List<String> args) {
        return new DocsButtonData(
            Long.parseLong(args.get(0)),
            args.get(1),
            Boolean.parseBoolean(args.get(2)),
            Boolean.parseBoolean(args.get(3))
        );
    }

    public List<String> toArguments() {
        return List.of(
            String.valueOf(buttonOwner),
            query,
            String.valueOf(shortDescription),
            String.valueOf(omitTags)
        );
    }

    public DocsButtonData withShortDescription(final boolean shortDescription) {
        return new DocsButtonData(buttonOwner(), query(), shortDescription, omitTags());
    }

    public DocsButtonData withOmitTags(final boolean omitTags) {
        return new DocsButtonData(buttonOwner(), query(), shortDescription(), omitTags);
    }
}
