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
package com.mcmoddev.mmdbot.core.event;

import com.mcmoddev.mmdbot.core.util.WarningDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WarningEvent {

    protected final WarningDocument document;

    public WarningEvent(final WarningDocument document) {
        this.document = document;
    }

    public WarningDocument getDocument() {
        return document;
    }

    public static final class Add extends WarningEvent {
        private static final List<Consumer<Add>> LISTENERS = Collections.synchronizedList(new ArrayList<>());

        public Add(WarningDocument doc) {
            super(doc);
        }

        public static void addListener(Consumer<Add> listener) {
            LISTENERS.add(listener);
        }

        public static void fire(Add event) {
            LISTENERS.forEach(c -> c.accept(event));
        }

    }

    public static final class Clear extends WarningEvent {
        private static final List<Consumer<Clear>> LISTENERS = Collections.synchronizedList(new ArrayList<>());

        private final long moderatorId;

        public Clear(WarningDocument doc, final long moderatorId) {
            super(doc);
            this.moderatorId = moderatorId;
        }

        public static void addListener(Consumer<Clear> listener) {
            LISTENERS.add(listener);
        }

        public static void fire(Clear event) {
            LISTENERS.forEach(c -> c.accept(event));
        }

        public long getModeratorId() {
            return moderatorId;
        }

        /**
         * @return the old warning document that was cleared
         */
        @Override
        public WarningDocument getDocument() {
            return super.getDocument();
        }
    }

}
