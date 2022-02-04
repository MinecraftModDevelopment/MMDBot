package com.mcmoddev.mmdbot.modules.commands.community;

import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.ALLOWED_MENTIONS;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createGuild;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createMember;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createMessageChannel;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createTextChannel;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.createUser;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.getEmbedFromValue;
import static com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils.validateArgs;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
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
import java.util.List;
import java.util.Objects;

public class CmdEvaluate extends SlashCommand {

    public CmdEvaluate() {
        guildOnly = true;
        name = "evaluate";
        aliases = new String[] {"eval"};
        help = "Evaluates the given script";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate.").setRequired(true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue(hook -> {
            try {
                ScriptingUtils.evaluate(Utils.getOrEmpty(event, "script"), createContext(new EvaluationContext() {
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
                }));
            } catch (ScriptingUtils.ScriptingException e) {
                hook.editOriginal("There was an exception evaluating: " + e.getLocalizedMessage()).queue();
            }
        });
    }

    @Override
    protected void execute(final CommandEvent event) {
        var script = event.getArgs();
        if (script.contains("```js") && script.endsWith("```")) {
            script = script.substring(script.indexOf("```js") + 5);
            script = script.substring(0, script.lastIndexOf("```"));
        }
        try {
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
            ScriptingUtils.evaluate(script, context);
        } catch (ScriptingUtils.ScriptingException e) {
            event.getMessage().reply("There was an exception evaluating: " + e.getLocalizedMessage()).queue();
        }
    }

    public static ScriptingContext createContext(EvaluationContext evalContext) {
        final var context = ScriptingContext.of("Evaluation");
        context.set("guild", evalContext.getGuild() == null ? null : createGuild(evalContext.getGuild()));
        context.set("member", evalContext.getMember() == null ? null : createMember(evalContext.getMember()));
        context.set("user", createUser(evalContext.getUser()));
        final var canSendEmbed = evalContext.getGuild() == null || evalContext.getMember().hasPermission(evalContext.getTextChannel(), Permission.MESSAGE_EMBED_LINKS);
        context.set("channel", createMessageChannel(evalContext.getMessageChannel(), true));
        context.set("textChannel", createTextChannel(evalContext.getTextChannel(), true));
        context.setFunctionVoid("reply", args -> {
            validateArgs(args, 1);
            evalContext.reply(args.get(0).asString());
        });
        if (canSendEmbed) {
            context.setFunctionVoid("replyEmbeds", args -> {
                evalContext.replyEmbeds(args.stream().map(ScriptingUtils::getEmbedFromValue)
                    .filter(Objects::nonNull).toArray(MessageEmbed[]::new));
            });
            context.setFunctionVoid("replyEmbed", args -> {
                validateArgs(args, 1);
                final var v = args.get(0);
                final var embed = getEmbedFromValue(v);
                if (embed != null) {
                    evalContext.replyEmbeds(embed);
                }
            });
        }
        return context;
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
