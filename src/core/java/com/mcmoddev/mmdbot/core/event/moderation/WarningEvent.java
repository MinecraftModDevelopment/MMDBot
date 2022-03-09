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
package com.mcmoddev.mmdbot.core.event.moderation;

import com.mcmoddev.mmdbot.core.util.WarningDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public sealed class WarningEvent extends ModerationEvent permits WarningEvent.Add, WarningEvent.Clear, WarningEvent.ClearAllWarns {

    protected final WarningDocument document;

    protected WarningEvent(final long guildId, final long moderatorId, final long targetId, final WarningDocument doc) {
        super(guildId, moderatorId, targetId);
        this.document = doc;
    }

    public WarningDocument getDocument() {
        return document;
    }

    public static final class Add extends WarningEvent {

        public Add(final long guildId, final long moderatorId, final long targetId, final WarningDocument doc) {
            super(guildId, moderatorId, targetId, doc);
        }
    }

    public static final class Clear extends WarningEvent {

        public Clear(final long guildId, final long moderatorId, final long targetId, final WarningDocument doc) {
            super(guildId, moderatorId, targetId, doc);
        }
        /**
         * @return the old warning document that was cleared
         */
        @Override
        public WarningDocument getDocument() {
            return super.getDocument();
        }
    }

    public static final class ClearAllWarns extends WarningEvent {
        public ClearAllWarns(final long guildId, final long moderatorId, final long targetId) {
            super(guildId, moderatorId, targetId, null);
        }
    }

}
