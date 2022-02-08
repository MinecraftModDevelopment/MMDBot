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
package com.mcmoddev.mmdbot.modules.commands.community;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.TaskScheduler;
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.ALLOWED_MENTIONS;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createGuild;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createMember;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createMessageChannel;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createTextChannel;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createUser;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.getEmbedFromValue;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.validateArgs;

public class CmdEvaluate extends SlashCommand {

    public CmdEvaluate() {
        guildOnly = true;
        name = "evaluate";
        aliases = new String[]{"eval"};
        help = "Evaluates the given script";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate.").setRequired(true));
    }

    public static final Set<Long> USED_CHANNELS = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue(hook -> {
            final var context = createContext(new EvaluationContext() {
                @Override
                public Guild getGuild() {
                    return event.getGuild();
                }

                @Override
                public TextChannel getTextChannel() {
                    return event.getTextChannel();
                }

                @Override
                public @NotNull MessageChannel getMessageChannel() {
                    return event.getChannel();
                }

                @Override
                public Member getMember() {
                    return event.getMember();
                }

                @Override
                public @NotNull User getUser() {
                    return event.getUser();
                }

                @Override
                public void reply(final String content) {
                    hook.editOriginal(new MessageBuilder(content).setAllowedMentions(ALLOWED_MENTIONS).build())
                        .setActionRow(DismissListener.createDismissButton(getUser())).queue();
                }

                @Override
                public void replyEmbeds(final MessageEmbed... embeds) {
                    hook.editOriginal(new MessageBuilder().setEmbeds(embeds).setAllowedMentions(ALLOWED_MENTIONS).build())
                        .setActionRow(DismissListener.createDismissButton(getUser())).queue();
                }
            });

            final var evalThread = new Thread(() -> {
                try {
                    ScriptingUtils.evaluate(Utils.getOrEmpty(event, "script"), context);
                } catch (ScriptingUtils.ScriptingException exception) {
                    hook.editOriginal("There was an exception evaluating "
                        + exception.getLocalizedMessage()).queue();
                }
            }, "ScriptEvaluation");
            evalThread.setDaemon(true);
            evalThread.start();
            TaskScheduler.scheduleTask(() -> {
                if (evalThread.isAlive()) {
                    evalThread.interrupt();
                    hook.editOriginal("Evaluation was timed out!").queue();
                }
            }, 4, TimeUnit.SECONDS);
        });
    }

    @Override
    protected void execute(final CommandEvent event) {
        var script = event.getArgs();
        if (script.contains("```js") && script.endsWith("```")) {
            script = script.substring(script.indexOf("```js") + 5);
            script = script.substring(0, script.lastIndexOf("```"));
        }
        final var context = createContext(new EvaluationContext() {
            @Override
            public Guild getGuild() {
                return event.getGuild();
            }

            @Override
            public TextChannel getTextChannel() {
                return event.getTextChannel();
            }

            @Override
            public @NotNull MessageChannel getMessageChannel() {
                return event.getChannel();
            }

            @Override
            public Member getMember() {
                return event.getMember();
            }

            @Override
            public @NotNull User getUser() {
                return event.getAuthor();
            }

            @Override
            public void reply(final String content) {
                event.getMessage().reply(new MessageBuilder(content).setAllowedMentions(ALLOWED_MENTIONS).build())
                    .setActionRow(DismissListener.createDismissButton(getUser())).mentionRepliedUser(false).queue();
            }

            @Override
            public void replyEmbeds(final MessageEmbed... embeds) {
                event.getMessage().reply(new MessageBuilder().setEmbeds(embeds).setAllowedMentions(ALLOWED_MENTIONS).build())
                    .setActionRow(DismissListener.createDismissButton(getUser())).mentionRepliedUser(false).queue();
            }
        });
        final var canEditMessage = event.getGuild() != null && event.getMember().hasPermission(Permission.MESSAGE_MANAGE);
        final var hasMsgReference = event.getMessage().getMessageReference() != null;
        if (canEditMessage && event.getTextChannel() != null) {
            context.setFunctionVoid("editMessage", args -> {
                if (hasMsgReference) {
                    validateArgs(args, 1);
                } else {
                    validateArgs(args, 2);
                }
                final var msgId = hasMsgReference ? event.getMessage().getMessageReference().getMessageIdLong() :
                    args.get(0).asLong();
                event.getTextChannel().editMessageById(msgId, args.get(hasMsgReference ? 0 : 1).asString()).queue();
            });
            context.setFunctionVoid("editMessageEmbeds", args -> {
                if (hasMsgReference) {
                    if (args.size() < 1) {
                        throw new IllegalArgumentException("Not enough arguments were provided!");
                    }
                } else {
                    if (args.size() < 2) {
                        throw new IllegalArgumentException("Not enough arguments were provided!");
                    }
                }
                final var msgId = hasMsgReference ? event.getMessage().getMessageReference().getMessageIdLong() :
                    args.get(0).asLong();
                final var embeds = args.subList(hasMsgReference ? 0 : 1, args.size() - 1).stream().map(ScriptingUtils::getEmbedFromValue)
                    .filter(Objects::nonNull).toArray(MessageEmbed[]::new);
                event.getTextChannel().editMessageEmbedsById(msgId, embeds).queue();
            });
        }
        final String finalScript = script;
        final var evalThread = new Thread(() -> {
            try {
                ScriptingUtils.evaluate(finalScript, context);
            } catch (ScriptingUtils.ScriptingException exception) {
                event.getMessage().reply("There was an exception evaluating: "
                    + exception.getLocalizedMessage()).queue();
            }
        }, "ScriptEvaluation");
        evalThread.setDaemon(true);
        evalThread.start();
        TaskScheduler.scheduleTask(() -> {
            if (evalThread.isAlive()) {
                evalThread.interrupt();
                event.getMessage().reply("Evaluation was timed out!").queue();
            }
        }, 4, TimeUnit.SECONDS);
    }

    public static ScriptingContext createContext(EvaluationContext evalContext) {
        final var context = ScriptingContext.of("Evaluation");
        context.set("guild", evalContext.getGuild() == null ? null : createGuild(evalContext.getGuild()));
        context.set("member", evalContext.getMember() == null ? null : createMember(evalContext.getMember()));
        context.set("user", createUser(evalContext.getUser()));
        final var canSendEmbed = evalContext.getGuild() == null || evalContext.getMember().hasPermission(evalContext.getTextChannel(), Permission.MESSAGE_EMBED_LINKS);
        context.set("channel", createMessageChannel(evalContext.getMessageChannel(), true)
            .setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                executeAndAddColldown(evalContext.getMessageChannel(), c -> c.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue());
            })
            .setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddColldown(evalContext.getMessageChannel(), c -> c.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue());
                }
            })
            .setFunctionVoid("sendEmbeds", args -> executeAndAddColldown(evalContext.getMessageChannel(), c -> c.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).toList()).allowedMentions(ALLOWED_MENTIONS).queue())));
        context.set("textChannel", createTextChannel(evalContext.getTextChannel(), true)
            .setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                executeAndAddColldown(evalContext.getTextChannel(), c -> c.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue());
            })
            .setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddColldown(evalContext.getTextChannel(), c -> c.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue());
                }
            })
            .setFunctionVoid("sendEmbeds", args -> executeAndAddColldown(evalContext.getTextChannel(), c -> c.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).toList()).allowedMentions(ALLOWED_MENTIONS).queue())));
        context.setFunctionVoid("reply", args -> {
            validateArgs(args, 1);
            executeAndAddColldown(evalContext.getMessageChannel(), c -> evalContext.reply(args.get(0).asString()));
        });
        if (canSendEmbed) {
            context.setFunctionVoid("replyEmbeds", args -> {
                executeAndAddColldown(evalContext.getMessageChannel(), c -> evalContext.replyEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                    .filter(Objects::nonNull).toArray(MessageEmbed[]::new)));
            });
            context.setFunctionVoid("replyEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddColldown(evalContext.getMessageChannel(), c -> evalContext.replyEmbeds(embed));
                }
            });
        }
        return context;
    }

    public static void executeAndAddColldown(MessageChannel channel, Consumer<MessageChannel> consumer) {
        if (!USED_CHANNELS.contains(channel.getIdLong())) {
            consumer.accept(channel);
            USED_CHANNELS.add(channel.getIdLong());
            TaskScheduler.scheduleTask(() -> USED_CHANNELS.remove(channel.getIdLong()), 3, TimeUnit.SECONDS);
        }
    }

    interface EvaluationContext {
        @Nullable
        Guild getGuild();

        @Nullable
        TextChannel getTextChannel();

        @Nonnull
        MessageChannel getMessageChannel();

        @Nullable
        Member getMember();

        @Nonnull
        User getUser();

        void reply(String content);

        void replyEmbeds(MessageEmbed... embeds);
    }
}
