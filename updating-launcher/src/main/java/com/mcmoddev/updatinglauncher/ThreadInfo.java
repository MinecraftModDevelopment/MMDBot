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
package com.mcmoddev.updatinglauncher;

import java.io.Serializable;

public record ThreadInfo(Group group, long id, String name, int priority, boolean daemon, Thread.State state, StackTraceElement[] stackElements) implements Serializable {

    public static ThreadInfo fromThread(Thread thread, StackTraceElement[] stackElements) {
        return new ThreadInfo(
            new Group(thread.getThreadGroup().getName()),
            thread.getId(),
            thread.getName(),
            thread.getPriority(),
            thread.isDaemon(),
            thread.getState(),
            stackElements
        );
    }

    public record Group(String name) implements Serializable {}

}
