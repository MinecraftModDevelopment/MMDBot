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
package com.mcmoddev.mmdbot.utilities.scripting.object;

import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.LinkedHashMap;

import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.validateArgs;

/**
 * How does {@code setAuthor} work:
 * - setAuthor(String name) -> sets the author's name
 * - setAuthor(String name, String url) -> sets the author's name and URL (click link)
 * - setAuthor(String name, String url, String imageUrl) -> sets the author's name, URL and icon (in this order). All the parameters can be null
 */
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
