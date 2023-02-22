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
package com.mcmoddev.mmdbot.core.event.moderation;

public class ScamLinkEvent extends ModerationEvent {
    private final long channelId;
    private final String messageContent;
    private final String targetAvatar;
    private final boolean editedMessage;

    public ScamLinkEvent(final long guildId, final long targetId, final long channelId, final String messageContent,
                         final String targetAvatar, final boolean editedMessage) {
        super(guildId, 0L, targetId);
        this.channelId = channelId;
        this.messageContent = messageContent;
        this.targetAvatar = targetAvatar;
        this.editedMessage = editedMessage;
    }

    @Override
    public long getModeratorId() {
        throw new UnsupportedOperationException();
    }

    public String getTargetAvatar() {
        return targetAvatar;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public long getChannelId() {
        return channelId;
    }

    public boolean isMessageEdited() {
        return editedMessage;
    }
}
