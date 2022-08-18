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
package com.mcmoddev.mmdbot.watcher.rules;

import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

public record RuleAgreementChecker(Supplier<JDA> jda) implements Runnable {
    @Override
    public void run() {
        final var jda = jda().get();
        if (jda == null) return;
        jda.getGuilds().forEach(guild -> {
            final var roleId = UpdateRulesCommand.getAcceptedRulesRole(guild.getIdLong());
            if (roleId != 0) {
                final var now = Instant.now();
                // 24h < joinTime < 48h
                guild.findMembers(it ->
                        it.getTimeJoined().toInstant().isBefore(now.minus(1, ChronoUnit.DAYS))
                        && it.getTimeJoined().toInstant().isAfter(now.minus(2, ChronoUnit.DAYS))
                        && !it.getUser().isBot()
                        && it.getRoles().stream().noneMatch(role -> role.getIdLong() == roleId)
                    )
                    .onSuccess(members -> {
                        if (!members.isEmpty()) {
                            RestAction.allOf(members.stream()
                                .map(m -> m.kick("Not agreeing to the rules"))
                                .toList())
                                .queue();
                        }
                    })
                    .onError(e -> TheWatcher.LOGGER.error("Encountered exception kicking users that have not agreed to the rules: ", e));
            }
        });
    }
}
