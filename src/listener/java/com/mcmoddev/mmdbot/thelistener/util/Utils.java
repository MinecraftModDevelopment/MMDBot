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
package com.mcmoddev.mmdbot.thelistener.util;

import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class Utils {

    public static String mentionAndID(final long id) {
        return "<@" + id + "> (" + id + ")";
    }

    public static void getAuditLog(final Guild guild, final long targetId, UnaryOperator<AuditLogPaginationAction> modifier, Consumer<AuditLogEntry> consumer) {
        getAuditLog(guild, targetId, modifier, consumer, () -> {
        });
    }

    public static void getAuditLog(final Guild guild, final long targetId, UnaryOperator<AuditLogPaginationAction> modifier, Consumer<AuditLogEntry> consumer, Runnable orElse) {
        modifier.apply(guild.retrieveAuditLogs())
            .queue(logs -> logs.stream()
                .filter(entry -> entry.getTargetIdLong() == targetId)
                .findFirst()
                .ifPresentOrElse(consumer, orElse));
    }

}
