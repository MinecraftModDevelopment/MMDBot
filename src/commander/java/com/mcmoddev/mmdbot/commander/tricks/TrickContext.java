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
package com.mcmoddev.mmdbot.commander.tricks;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Context used for running tricks.
 *
 * @author matyrobbrt
 */
public interface TrickContext {

    EnumSet<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(Message.MentionType.CHANNEL, Message.MentionType.EMOJI);

    @Nullable
    Member getMember();

    @Nonnull
    User getUser();

    @Nonnull
    MessageChannel getChannel();

    @Nullable
    TextChannel getTextChannel();

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

        @Nullable
        @Override
        public TextChannel getTextChannel() {
            return event.getChannel().getType() == ChannelType.TEXT ? event.getTextChannel() : null;
        }

        @NotNull
        @Override
        public MessageChannel getChannel() {
            return event.getChannel();
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

        @Nullable
        @Override
        public TextChannel getTextChannel() {
            return event.getChannelType() == ChannelType.TEXT ? event.getTextChannel() : null;
        }

        @NotNull
        @Override
        public MessageChannel getChannel() {
            return event.getChannel();
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
                .setActionRow(DismissListener.createDismissButton(getUser(), event().getMessage())).mentionRepliedUser(false).queue();
        }

        @Override
        public void replyEmbeds(final MessageEmbed... embeds) {
            event.getMessage().reply(new MessageBuilder().setEmbeds(embeds).setAllowedMentions(ALLOWED_MENTIONS).build())
                .setActionRow(DismissListener.createDismissButton(getUser(), event.getMessage())).mentionRepliedUser(false).queue();
        }

        @Override
        public void replyWithMessage(final Message message) {
            event.getMessage().reply(message).setActionRow(DismissListener.createDismissButton(getUser(), event.getMessage())).queue();
        }
    }

    record DelegateWithArguments(@Nonnull TrickContext context, @Nonnull String[] args) implements TrickContext {

        @Nullable
        @Override
        public Member getMember() {
            return context.getMember();
        }

        @NotNull
        @Override
        public User getUser() {
            return context.getUser();
        }

        @NotNull
        @Override
        public MessageChannel getChannel() {
            return context.getChannel();
        }

        @Nullable
        @Override
        public TextChannel getTextChannel() {
            return context.getTextChannel();
        }

        @Nullable
        @Override
        public Guild getGuild() {
            return context.getGuild();
        }

        @NotNull
        @Override
        public String[] getArgs() {
            return args;
        }

        @Override
        public void reply(final String content) {
            context.reply(content);
        }

        @Override
        public void replyEmbeds(final MessageEmbed... embeds) {
            context.replyEmbeds(embeds);
        }

        @Override
        public void replyWithMessage(final Message message) {
            context.replyWithMessage(message);
        }
    }
}
