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
package com.mcmoddev.updatinglauncher.api;

import java.lang.instrument.Instrumentation;

/**
 * This interface listens to the status of a process managed by a launcher. <br>
 * {@link java.util.ServiceLoader Service loaders} are used for this system. The service path
 * is {@code com.mcmoddev.updatinglauncher.api.StatusListener}.
 *
 * @since 1.3.0
 */
public interface StatusListener {

    /**
     * Called before a process is shut down.
     */
    default void onShutdown() {
    }

    /**
     * Called before a process is started up. Precisely,
     * this method is called by the agent, after the RMI connection has been started.
     */
    default void onStartup() {
    }

    /**
     * This method is called by the agent in its premain method before the RMI connection
     * is established with the launcher.
     *
     * @param instrumentation the instrumentation
     */
    default void premain(Instrumentation instrumentation) {
    }

}
