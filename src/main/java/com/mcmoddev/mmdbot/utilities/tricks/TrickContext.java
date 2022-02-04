package com.mcmoddev.mmdbot.utilities.tricks;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public interface TrickContext {

    EnumSet<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE);

    @Nullable
    Member getMember();

    @Nonnull
    User getUser();

    @Nonnull
    TextChannel getChannel();

    @Nullable
    Guild getGuild();

    @Nonnull
    String[] getArgs();

    void reply(String content);
    void replyEmbeds(MessageEmbed... embeds);
    void replyWithMessage(Message message);

    record Slash(SlashCommandEvent event, InteractionHook hook, String[] args) implements TrickContext {

        @Nullable
        @Override
        public Member getMember() {
            return event.getMember();
        }

        @NotNull
        @Override
        public User getUser() {
            return event.getUser();
        }

        @NotNull
        @Override
        public TextChannel getChannel() {
            return event.getTextChannel();
        }

        @Nullable
        @Override
        public Guild getGuild() {
            return event.getGuild();
        }

        @NotNull
        @Override
        public String[] getArgs() {
            return args;
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

        @Override
        public void replyWithMessage(final Message message) {
            hook.editOriginal(message).setActionRow(DismissListener.createDismissButton(getUser())).queue();
        }
    }

    record Normal(CommandEvent event, String[] args) implements TrickContext {

        @Nullable
        @Override
        public Member getMember() {
            return event.getMember();
        }

        @NotNull
        @Override
        public User getUser() {
            return event.getMessage().getAuthor();
        }

        @NotNull
        @Override
        public TextChannel getChannel() {
            return event.getTextChannel();
        }

        @Nullable
        @Override
        public Guild getGuild() {
            return event.getGuild();
        }

        @NotNull
        @Override
        public String[] getArgs() {
            return args;
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
        public void replyWithMessage(final Message message) {
            event.getMessage().reply(message).setActionRow(DismissListener.createDismissButton(getUser())).queue();
        }
    }
}
