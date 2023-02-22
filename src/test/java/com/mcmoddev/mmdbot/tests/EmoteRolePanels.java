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
package com.mcmoddev.mmdbot.tests;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jetbrains.annotations.Nullable;

public interface EmoteRolePanels {

    @Nullable
    @SqlQuery("select role from role_panels where channel = :channel and message = :message and emote = :emote")
    Long getRole(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

    @Nullable
    @SqlQuery("select permanent from role_panels where channel = :channel and message = :message and emote = :emote")
    Boolean isPermanent(@Bind("channel") long channel, @Bind("message") long message, @Bind("emote") String emote);

}
