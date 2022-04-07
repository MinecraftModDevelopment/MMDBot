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
package com.mcmoddev.mmdbot.core.util.builder;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.github.matyrobbrt.curseforgeapi.util.ExceptionConsumer;
import lombok.Builder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder(builderClassName = "Builder")
public class SlashCommandBuilder {

    public static final class Builder {

        private final List<SlashCommand> children = new ArrayList<>();

        public Builder aliases(String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder children(SlashCommand... children) {
            this.children.addAll(Arrays.asList(children));
            return this;
        }

        public Builder children(Builder... children) {
            this.children.addAll(Stream.of(children).map(Builder::build).toList());
            return this;
        }

        public Builder userPermissions(Permission... permissions) {
            this.userPermissions = permissions;
            return this;
        }

        public Builder options(OptionData... options) {
            this.options = List.of(options);
            return this;
        }

        public SlashCommand build() {
            return new Cmd() {
                private final Map<String, SlashCommand> byNameMap = Builder.this.children.stream()
                    .collect(Collectors.toMap(Command::getName, Function.identity()));

                @Override
                public void init() {
                    this.name = Builder.this.name;
                    this.aliases = Builder.this.aliases == null ? new String[0] : Builder.this.aliases;
                    this.guildOnly = Builder.this.guildOnly;
                    this.userPermissions = Builder.this.userPermissions == null ? new Permission[]{} : Builder.this.userPermissions;
                    this.children = Builder.this.children.toArray(SlashCommand[]::new);
                    this.help = Builder.this.help == null ? "No help available." : Builder.this.help;
                    this.options = Builder.this.options == null ? List.of() : Builder.this.options;
                }

                @Override
                protected void execute(final SlashCommandEvent event) {
                    if (Builder.this.executes != null) {
                        Builder.this.executes.accept(event);
                    }
                }

                @Override
                public void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
                    if (event.getSubcommandName() != null) {
                        final var cmd = byNameMap.get(event.getSubcommandName());
                        if (cmd != null) {
                            cmd.onAutoComplete(event);
                        }
                    }
                    if (onAutocomplete != null) {
                        onAutocomplete.accept(event);
                    }
                }
            };
        }
    }

    private final String name;
    private final String[] aliases;
    private final Permission[] userPermissions;
    private final String help;
    private final List<OptionData> options;
    private boolean guildOnly;
    private ExceptionConsumer<SlashCommandEvent, ?> executes;
    private Consumer<CommandAutoCompleteInteractionEvent> onAutocomplete;

    private static abstract class Cmd extends SlashCommand {
        public Cmd() {
            init();
        }

        public abstract void init();
    }
}
