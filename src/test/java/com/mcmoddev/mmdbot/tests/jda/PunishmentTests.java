/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.tests.jda;

import com.mcmoddev.mmdbot.core.common.ScamDetector;
import com.mcmoddev.mmdbot.watcher.punishments.PunishableAction;
import com.mcmoddev.mmdbot.watcher.punishments.PunishableActions;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandReference;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.entities.UserImpl;
import org.apache.commons.collections4.Bag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class PunishmentTests {

    public static final long MEMBER_ID = 160272L;

    private static MemberImpl member;

    @Test
    void testPhishingLinks() {
        final var message = mock(ReceivedMessage.class);

        when(message.getContentRaw()).thenReturn("https://dscord.com/scam");
        when(message.getChannel()).thenReturn(new CastingChannelUnion(JDATesting.textChannel));
        when(message.getMember()).thenReturn(member);

        Assertions.assertTrue(
            testAction(
                PunishableActions.SCAM_LINK,
                new MessageReceivedEvent(
                    JDATesting.jda,
                    1L,
                    message
                )
            ),
            "Phishing link was not found!"
        );
    }

    @Test
    void testNewAccount() {
        Assertions.assertTrue(
            testAction(
                PunishableActions.NEW_ACCOUNT,
                new GuildMemberJoinEvent(
                    JDATesting.jda,
                    1L,
                    member
                )
            ),
            "New account was not detected!"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMassPinging() {
        final var message = mock(ReceivedMessage.class);

        final var set = mock(Set.class);
        when(set.size()).thenReturn(20);

        final var bag = mock(Bag.class);
        when(bag.uniqueSet()).thenReturn(set);

        final var mentions = new Mentions() {

            @NotNull
            @Override
            public JDA getJDA() {
                return JDATesting.jda;
            }

            @Override
            public boolean mentionsEveryone() {
                return false;
            }

            @NotNull
            @Override
            public List<User> getUsers() {
                return List.of();
            }

            @NotNull
            @Override
            public Bag<User> getUsersBag() {
                return bag;
            }

            @NotNull
            @Override
            public List<GuildChannel> getChannels() {
                return List.of();
            }

            @NotNull
            @Override
            public Bag<GuildChannel> getChannelsBag() {
                return bag;
            }

            @NotNull
            @Override
            public <T extends GuildChannel> List<T> getChannels(@NotNull final Class<T> clazz) {
                return List.of();
            }

            @NotNull
            @Override
            public <T extends GuildChannel> Bag<T> getChannelsBag(@NotNull final Class<T> clazz) {
                return bag;
            }

            @NotNull
            @Override
            public List<Role> getRoles() {
                return List.of();
            }

            @NotNull
            @Override
            public Bag<Role> getRolesBag() {
                return bag;
            }

            @NotNull
            @Override
            public List<CustomEmoji> getCustomEmojis() {
                return List.of();
            }

            @NotNull
            @Override
            public Bag<CustomEmoji> getCustomEmojisBag() {
                return bag;
            }

            @NotNull
            @Override
            public List<Member> getMembers() {
                return List.of();
            }

            @NotNull
            @Override
            public Bag<Member> getMembersBag() {
                return bag;
            }

            @NotNull
            @Override
            public List<IMentionable> getMentions(@NotNull final Message.MentionType @NotNull ... types) {
                return List.of();
            }

            @Override
            public boolean isMentioned(@NotNull final IMentionable mentionable, @NotNull final Message.MentionType @NotNull ... types) {
                return false;
            }

            @NotNull
            @Override
            public Bag<SlashCommandReference> getSlashCommandsBag() {
                return bag;
            }

            @NotNull
            @Override
            public List<SlashCommandReference> getSlashCommands() {
                return List.of();
            }
        };

        when(message.getMentions()).thenReturn(mentions);
        when(message.getChannel()).thenReturn(new CastingChannelUnion(JDATesting.textChannel));
        when(message.getMember()).thenReturn(member);

        Assertions.assertTrue(
            testAction(
                PunishableActions.SPAM_PING,
                new MessageReceivedEvent(
                    JDATesting.jda,
                    1L,
                    message
                )
            ),
            "New account was not detected!"
        );
    }

    @BeforeAll
    static void setup() {
        ScamDetector.setupScamLinks();
        JDATesting.setup();

        member = spy(new MemberImpl(JDATesting.guild, new UserImpl(MEMBER_ID, JDATesting.jda)));
        when(member.getTimeCreated()).thenReturn(OffsetDateTime.now());
    }

    private static boolean testAction(PunishableActions action, Object event) {
        return testAction(action.getAction(), event);
    }

    @SuppressWarnings("unchecked")
    private static <T extends GenericEvent> boolean testAction(PunishableAction<T> action, Object event) {
        return action.test((T) event);
    }
}
