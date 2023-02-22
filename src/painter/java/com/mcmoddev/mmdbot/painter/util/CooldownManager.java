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
package com.mcmoddev.mmdbot.painter.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private final Cache<Long, Instant> users;
    private final Duration cooldownTime;

    public CooldownManager(final long time, final TimeUnit unit) {
        this.cooldownTime = Duration.of(time, unit.toChronoUnit());
        this.users = Caffeine.newBuilder()
            .expireAfterWrite(time, unit)
            .build();
    }

    public boolean check(final User user) {
        if (users.asMap().containsKey(user.getIdLong())) {
            return false;
        }
        users.put(user.getIdLong(), Instant.now());
        return true;
    }

    public String coolDownFriendly(final User user) {
        return TimeFormat.RELATIVE.format(Objects.requireNonNull(users.getIfPresent(user.getIdLong()))
            .plus(cooldownTime));
    }
}
