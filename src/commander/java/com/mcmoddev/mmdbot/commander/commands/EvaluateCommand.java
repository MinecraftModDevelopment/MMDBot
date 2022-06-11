/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.tricks.TrickContext;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.util.script.ScriptingContext;
import com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import com.mcmoddev.mmdbot.core.util.gist.GistUtils;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mcmoddev.mmdbot.core.util.event.DismissListener.createDismissButton;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.ALLOWED_MENTIONS;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.createGuild;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.createMessageChannel;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.createTextChannel;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.getEmbedFromValue;
import static com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils.validateArgs;

public class EvaluateCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final EvaluateCommand COMMAND = new EvaluateCommand();

    private static final ExecutorService EVALUATION_EXECUTOR = Utils.makeWithSupplier(() -> {
        final var group = new ThreadGroup("ScriptingEvaluation");
        final var ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r -> com.mcmoddev.mmdbot.core.util.Utils.setThreadDaemon(new Thread(group,
            r, "ScriptingEvaluator #" + group.activeCount()), true));
        ex.setKeepAliveTime(10, TimeUnit.MINUTES);
        ex.allowCoreThreadTimeOut(true);
        return ex;
    });

    private EvaluateCommand() {
        guildOnly = true;
        name = "evaluate";
        aliases = new String[]{"eval"};
        help = "Evaluates the given script";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate."));
    }

    public static final Set<Long> USED_CHANNELS = Collections.synchronizedSet(new HashSet<>());
    public static final String THREAD_INTERRUPTED_MESSAGE = "org.graalvm.polyglot.PolyglotException: Thread was interrupted.";

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!TheCommander.getInstance().getGeneralConfig().features().isEvaluationEnabled()) {
            event.deferReply(true).setContent("Evaluation is not enabled!").queue();
            return;
        }
        final var scriptOption = event.getOption("script");
        if (scriptOption != null) {
            event.deferReply().allowedMentions(ALLOWED_MENTIONS)
                .queue(hook -> {
                    final var context = createInteractionContext(hook);

                    final var future = EVALUATION_EXECUTOR.submit(() -> {
                        try {
                            ScriptingUtils.evaluate(scriptOption.getAsString(), context);
                        } catch (ScriptingUtils.ScriptingException exception) {
                            if (exception.getMessage().equalsIgnoreCase(THREAD_INTERRUPTED_MESSAGE)) {
                                return;
                            }
                            hook.editOriginal("There was an exception evaluating "
                                + exception.getLocalizedMessage())
                                .setActionRow(createDismissButton())
                                .queue();
                        }
                    });
                    TaskScheduler.scheduleTask(() -> {
                        if (!future.isDone()) {
                            future.cancel(true);
                            hook.editOriginal("Evaluation was timed out!").setActionRow(createDismissButton()).queue();
                        }
                    }, 4, TimeUnit.SECONDS);
                });
        } else {
            final var scriptInput = TextInput.create("script", "Script", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setPlaceholder("The script to evaluate.")
                .setRequiredRange(1, TextInput.MAX_VALUE_LENGTH)
                .build();
            final var modal = Modal.create(ModalListener.MODAL_ID, "Evaluate a script")
                .addActionRow(scriptInput)
                .build();
            event.replyModal(modal).queue();
        }
    }

    public static final class ModalListener extends ListenerAdapter {
        public static final String MODAL_ID = "evaluate";

        @Override
        public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
            if (event.getModalId().equals(MODAL_ID)) {
                event.deferReply().allowedMentions(ALLOWED_MENTIONS).queue(hook -> {
                    final var context = createInteractionContext(hook);

                    final var future = EVALUATION_EXECUTOR.submit(() -> {
                        try {
                            ScriptingUtils.evaluate(Objects.requireNonNull(event.getValue("script")).getAsString(), context);
                        } catch (ScriptingUtils.ScriptingException exception) {
                            if (exception.getMessage().equalsIgnoreCase(THREAD_INTERRUPTED_MESSAGE)) {
                                return;
                            }
                            hook.editOriginal("There was an exception evaluating "
                                + exception.getLocalizedMessage())
                                .setActionRow(createDismissButton(event))
                                .queue();
                        }
                    });
                    TaskScheduler.scheduleTask(() -> {
                        if (!future.isDone()) {
                            future.cancel(true);
                            hook.editOriginal("Evaluation was timed out!")
                                .setActionRow(createDismissButton(event)).queue();
                        }
                    }, 4, TimeUnit.SECONDS);
                });
            }
        }
    }

    public static ScriptingContext createInteractionContext(final InteractionHook hook) {
        return createContext(new EvaluationContext() {
            @Override
            public Guild getGuild() {
                return hook.getInteraction().getGuild();
            }

            @Override
            public TextChannel getTextChannel() {
                return hook.getInteraction().getMessageChannel().getType() == ChannelType.TEXT ? hook.getInteraction().getTextChannel() : null;
            }

            @Override
            public @NotNull MessageChannel getMessageChannel() {
                return hook.getInteraction().getMessageChannel();
            }

            @Override
            public Member getMember() {
                return hook.getInteraction().getMember();
            }

            @Override
            public @NotNull User getUser() {
                return hook.getInteraction().getUser();
            }

            @Override
            public void reply(final String content) {
                hook.editOriginal(new MessageBuilder(content).setAllowedMentions(ALLOWED_MENTIONS).build())
                    .setActionRow(DismissListener.createDismissButton(hook.getInteraction().getUser()))
                    .queue();
            }

            @Override
            public void replyEmbeds(final MessageEmbed... embeds) {
                hook.editOriginal(new MessageBuilder().setEmbeds(embeds).setAllowedMentions(ALLOWED_MENTIONS).build())
                    .setActionRow(DismissListener.createDismissButton(hook.getInteraction().getUser()))
                    .queue();
            }

            @Override
            public void replyWithMessage(final Message msg) {
                hook.editOriginal(msg)
                    .setActionRow(DismissListener.createDismissButton(hook.getInteraction().getUser()))
                    .queue();
            }
        });
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!TheCommander.getInstance().getGeneralConfig().features().isEvaluationEnabled()) {
            event.reply("Evaluation is not enabled!");
            return;
        }

        var script = event.getArgs();
        if (script.contains("```js") && script.endsWith("```")) {
            script = script.substring(script.indexOf("```js") + 5);
            script = script.substring(0, script.lastIndexOf("```"));
        }
        if (!event.getMessage().getAttachments().isEmpty()) {
            for (var attach : event.getMessage().getAttachments()) {
                if (Objects.equals(attach.getFileExtension(), "js")) {
                    try {
                        script = GistUtils.readInputStream(attach.getProxy().download().get());
                        break;
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        final var context = createContext(new EvaluationContext() {
            @Override
            public Guild getGuild() {
                return event.getGuild();
            }

            @Override
            public TextChannel getTextChannel() {
                return event.isFromType(ChannelType.TEXT) ? event.getTextChannel() : null;
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

            @Override
            public void replyWithMessage(final Message msg) {
                event.getMessage().reply(msg).allowedMentions(ALLOWED_MENTIONS)
                    .setActionRow(DismissListener.createDismissButton(getUser())).mentionRepliedUser(false).queue();
            }
        });
        final var canEditMessage = event.getGuild() != null && event.getMember().hasPermission(Permission.MESSAGE_MANAGE);
        final var hasMsgReference = event.getMessage().getMessageReference() != null;
        if (canEditMessage && event.getChannel() instanceof GuildChannel) {
            context.setFunctionVoid("editMessage", args -> {
                if (hasMsgReference) {
                    validateArgs(args, 1);
                } else {
                    validateArgs(args, 2);
                }
                final var msgId = hasMsgReference ? event.getMessage().getMessageReference().getMessageIdLong() :
                    args.get(0).asLong();
                event.getChannel().editMessageById(msgId, args.get(hasMsgReference ? 0 : 1).asString()).queue();
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
                event.getChannel().editMessageEmbedsById(msgId, embeds).queue();
            });
        }
        final String finalScript = script;
        final var future = EVALUATION_EXECUTOR.submit(() -> {
            try {
                ScriptingUtils.evaluate(finalScript, context);
            } catch (ScriptingUtils.ScriptingException exception) {
                if (exception.getMessage().equalsIgnoreCase(THREAD_INTERRUPTED_MESSAGE)) {
                    return;
                }
                event.getMessage().reply("There was an exception evaluating: "
                        + exception.getLocalizedMessage()).allowedMentions(ALLOWED_MENTIONS)
                    .setActionRow(DismissListener.createDismissButton(event.getAuthor())).queue();
            }
        });
        TaskScheduler.scheduleTask(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                event.getMessage().reply("Evaluation was timed out!")
                    .setActionRow(DismissListener.createDismissButton(event.getAuthor())).queue();
            }
        }, 4, TimeUnit.SECONDS);
    }

    public static ScriptingContext createContext(EvaluationContext evalContext) {
        final var context = ScriptingContext.of("Evaluation");
        context.set("guild", evalContext.getGuild() == null ? null : createGuild(evalContext.getGuild()));
        context.set("member", evalContext.getMember() == null ? null : createMember(evalContext.getMember(), true));
        context.set("user", createUser(evalContext.getUser(), true));
        final var canSendEmbed = evalContext.getMessageChannel() instanceof GuildChannel guildChannel && evalContext.getMember().hasPermission(guildChannel, Permission.MESSAGE_EMBED_LINKS);
        context.set("channel", createMessageChannel(evalContext.getMessageChannel(), true)
            .setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                executeAndAddCooldown(evalContext.getMessageChannel(), c -> c.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue());
            })
            .setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddCooldown(evalContext.getMessageChannel(), c -> c.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue());
                }
            })
            .setFunctionVoid("sendEmbeds", args -> executeAndAddCooldown(evalContext.getMessageChannel(), c -> c.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).limit(3).toList()).allowedMentions(ALLOWED_MENTIONS).queue())));
        context.set("textChannel", evalContext.getTextChannel() == null ? null : createTextChannel(evalContext.getTextChannel(), true)
            .setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                executeAndAddCooldown(evalContext.getTextChannel(), c -> c.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue());
            })
            .setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddCooldown(evalContext.getTextChannel(), c -> c.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue());
                }
            })
            .setFunctionVoid("sendEmbeds", args -> executeAndAddCooldown(evalContext.getTextChannel(), c -> c.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).toList()).allowedMentions(ALLOWED_MENTIONS).queue())));
        context.setFunctionVoid("reply", args -> {
            validateArgs(args, 1);
            executeAndAddCooldown(evalContext.getMessageChannel(), c -> evalContext.reply(args.get(0).asString()));
        });
        if (canSendEmbed) {
            context.setFunctionVoid("replyEmbeds", args -> {
                executeAndAddCooldown(evalContext.getMessageChannel(), c -> evalContext.replyEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                    .filter(Objects::nonNull).limit(3).toArray(MessageEmbed[]::new)));
            });
            context.setFunctionVoid("replyEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    executeAndAddCooldown(evalContext.getMessageChannel(), c -> evalContext.replyEmbeds(embed));
                }
            });
        }
        context.setFunctionVoid("runTrick", args -> {
            validateArgs(args, 1, 2);
            final String[] trickArgs = args.size() > 1 ? args.get(1).as(String[].class) : new String[]{};
            Tricks.getTrick(args.get(0).asString()).ifPresent(trick -> trick.execute(new TrickContext() {
                @Nullable
                @Override
                public Member getMember() {
                    return evalContext.getMember();
                }

                @NotNull
                @Override
                public User getUser() {
                    return evalContext.getUser();
                }

                @NotNull
                @Override
                public MessageChannel getChannel() {
                    return evalContext.getMessageChannel();
                }

                @Nullable
                @Override
                public TextChannel getTextChannel() {
                    return evalContext.getTextChannel();
                }

                @Nullable
                @Override
                public Guild getGuild() {
                    return evalContext.getGuild();
                }

                @Nonnull
                @Override
                public String[] getArgs() {
                    return trickArgs;
                }

                @Override
                public void reply(final String content) {
                    evalContext.reply(content);
                }

                @Override
                public void replyEmbeds(final MessageEmbed... embeds) {
                    evalContext.replyEmbeds(embeds);
                }

                @Override
                public void replyWithMessage(final Message message) {
                    evalContext.replyWithMessage(message);
                }
            }));
        });
        return context;
    }

    public static ScriptingContext createMember(Member member, boolean canDm) {
        return ScriptingUtils.createMember(member, canDm)
            .set("user", createUser(member.getUser(), canDm));
    }

    public static ScriptingContext createUser(final User user, final boolean canDm) {
        return ScriptingUtils.createUser(user, canDm)
            .setFunction("openPrivateChannel", args -> {
                validateArgs(args, 0);
                final var privateChannel = user.openPrivateChannel().complete();
                return privateChannel == null ? null : overwriteDm(privateChannel, canDm);
            });
    }

    public static ScriptingContext overwriteDm(final MessageChannel channel, final boolean canDm) {
        final var context = ScriptingUtils.createMessageChannel(channel, canDm);
        if (canDm) {
            context.setFunctionVoid("sendMessage", args -> {
                validateArgs(args, 1);
                executeAndAddCooldown(channel, ch -> {
                    ch.sendMessage(args.get(0).asString()).allowedMentions(ALLOWED_MENTIONS).queue();
                });
            });
            context.setFunctionVoid("sendEmbed", args -> {
                validateArgs(args, 1);
                executeAndAddCooldown(channel, ch -> {
                    final var v = args.get(0);
                    final var embed = getEmbedFromValue(v);
                    if (embed != null) {
                        channel.sendMessageEmbeds(embed).allowedMentions(ALLOWED_MENTIONS).queue();
                    }
                });
            });
            context.setFunctionVoid("sendEmbeds", args -> executeAndAddCooldown(channel, ch -> ch.sendMessageEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                .filter(Objects::nonNull).limit(3).toList()).allowedMentions(ALLOWED_MENTIONS).queue()));
        }
        return context;
    }

    public static void executeAndAddCooldown(MessageChannel channel, Consumer<MessageChannel> consumer) {
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

        void replyWithMessage(Message msg);
    }
}
