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
package com.mcmoddev.mmdbot.core.util.jda.caching;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MessageData {

    String getId();
    long getIdLong();
    String getContent();

    long getAuthorId();
    String getAuthorUsername();
    String getAuthorAvatar();

    long getChannelId();
    List<String> getAttachments();
    @Nullable
    InteractionData getInteraction();

    static MessageData from(Message message) {
        return new MessageData() {
            final long id = message.getIdLong();
            final String content = message.getContentRaw();
            final long authorId = message.getAuthor().getIdLong();
            final long channelId = message.getChannel().getIdLong();
            final List<String> attachments = message.getAttachments().isEmpty() ? List.of() : message
                .getAttachments()
                .stream()
                .map(Message.Attachment::getUrl)
                .toList();
            final String authorName = message.getAuthor().getName();
            final String authorAvatar = message.getAuthor().getAvatarUrl();
            final InteractionData interactionData = message.getInteraction() == null ? null :
                new InteractionData() {
                    final long authorId = message.getInteraction().getUser().getIdLong();
                    @Override
                    public long getAuthorId() {
                        return authorId;
                    }
                };

            @Override
            public String getId() {
                return Long.toUnsignedString(id);
            }

            @Override
            public long getIdLong() {
                return id;
            }

            @Override
            public String getContent() {
                return content;
            }

            @Override
            public long getAuthorId() {
                return authorId;
            }

            @Override
            public long getChannelId() {
                return channelId;
            }

            @Override
            public List<String> getAttachments() {
                return attachments;
            }

            @Override
            public @Nullable InteractionData getInteraction() {
                return interactionData;
            }

            @Override
            public String getAuthorAvatar() {
                return authorAvatar;
            }

            @Override
            public String getAuthorUsername() {
                return authorName;
            }
        };
    }

}
