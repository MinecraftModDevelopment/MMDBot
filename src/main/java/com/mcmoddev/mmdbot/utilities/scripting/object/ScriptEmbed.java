package com.mcmoddev.mmdbot.utilities.scripting.object;

import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.validateArgs;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.LinkedHashMap;

public class ScriptEmbed extends ScriptingContext {

    private final EmbedBuilder builder;

    public ScriptEmbed() {
        this(new EmbedBuilder());
    }

    public ScriptEmbed(EmbedBuilder builder) {
        this("embed", builder);
    }

    public ScriptEmbed(String name, EmbedBuilder initialEmbed) {
        super(name, new LinkedHashMap<>());
        this.builder = initialEmbed;

        setFunction("setTitle", args -> {
            validateArgs(args, 1, 2);
            if (args.size() == 1) {
                builder.setTitle(args.get(0).asString());
            } else {
                builder.setTitle(args.get(0).asString(), args.get(1).asString());
            }
            return this;
        });
        setFunction("setDescription", args -> {
            validateArgs(args, 1);
            builder.setDescription(args.get(0).asString());
            return this;
        });
        setFunction("addField", args -> {
            validateArgs(args, 2, 3);
            if (args.size() == 2) {
                builder.addField(args.get(0).asString(), args.get(1).asString(), false);
            }
            if (args.size() == 3) {
                builder.addField(args.get(0).asString(), args.get(1).asString(), args.get(2).asBoolean());
            }
            return this;
        });
        setFunction("setAuthor", args -> {
            validateArgs(args, 1, 2, 3);
            if (args.size() == 1) {
                builder.setAuthor(args.get(0).asString());
            } else if (args.size() == 2) {
                builder.setAuthor(args.get(0).asString(), args.get(1).asString());
            } else if (args.size() == 3) {
                builder.setAuthor(args.get(0).asString(), args.get(1).asString(), args.get(2).asString());
            }
            return this;
        });
        setFunction("setColor", args -> {
            validateArgs(args, 1);
            builder.setColor(args.get(0).asInt());
            return this;
        });
        setFunction("setThumbnail", args -> {
            validateArgs(args, 1);
            builder.setThumbnail(args.get(0).asString());
            return this;
        });
        setFunction("setImage", args -> {
            validateArgs(args, 1);
            builder.setImage(args.get(0).asString());
            return this;
        });
        setFunction("setFooter", args -> {
            validateArgs(args, 1, 2);
            if (args.size() == 1) {
                builder.setFooter(args.get(0).asString());
            } else {
                builder.setFooter(args.get(0).asString(), args.get(1).asString());
            }
            return this;
        });
        setFunction("appendDescription", args -> {
            validateArgs(args, 1);
            builder.appendDescription(args.get(0).asString());
            return this;
        });
        setFunction("setTimestamp", args -> {
            validateArgs(args, 1);
            builder.setTimestamp(args.get(0).asInstant());
            return this;
        });
        setFunction("addBlankField", args -> {
            validateArgs(args, 0, 1);
            if (args.size() == 1) {
                builder.addBlankField(args.get(0).asBoolean());
            } else {
                builder.addBlankField(false);
            }
            return this;
        });

        setFunction("build", args -> builder.build());
    }

    public MessageEmbed build() {
        return builder.build();
    }
}
