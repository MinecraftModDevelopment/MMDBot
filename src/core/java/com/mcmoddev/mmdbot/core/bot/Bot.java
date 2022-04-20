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
package com.mcmoddev.mmdbot.core.bot;

import org.slf4j.Logger;

import java.io.IOException;

public interface Bot {

    void start();

    BotType<?> getType();

    default Logger getLogger() {
        return getType().getLogger();
    }

    void shutdown();

    /**
     * Called when the bot is allowed to migrate any old data. <br>
     * This method is <strong>always</strong> be called before {@link #start()}.
     */
    default void migrateData() throws IOException {

    }

    /**
     * @return if the bot blocks the startup thread.
     */
    default boolean blocksStartupThread() {
        return false;
    }
}
