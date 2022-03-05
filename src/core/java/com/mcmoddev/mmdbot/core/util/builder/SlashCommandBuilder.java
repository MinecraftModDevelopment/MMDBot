package com.mcmoddev.mmdbot.core.util.builder;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.github.matyrobbrt.curseforgeapi.util.ExceptionConsumer;
import lombok.Builder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Builder(builderClassName = "Builder")
public class SlashCommandBuilder {

    public static final class Builder {

        private final List<SlashCommand> children = new ArrayList<>();

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
                @Override
                public void init() {
                    this.name = Builder.this.name;
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
            };
        }
    }

    private final String name;
    private final Permission[] userPermissions;
    private final String help;
    private final List<OptionData> options;
    private boolean guildOnly;
    private ExceptionConsumer<SlashCommandEvent, ?> executes;

    private static abstract class Cmd extends SlashCommand {
        public Cmd() {
            init();
        }

        public abstract void init();
    }
}
