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
package com.mcmoddev.mmdbot.core;

import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.ServerBridge;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketContext;
import com.mcmoddev.mmdbot.dashboard.packets.CheckAuthorizedPacket;
import com.mcmoddev.mmdbot.dashboard.server.DashboardSever;
import com.mcmoddev.mmdbot.dashboard.util.Credentials;
import com.mcmoddev.mmdbot.dashboard.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j(topic = "DashboardActions")
public final class ServerBridgeImpl implements ServerBridge {

    @Override
    public CheckAuthorizedPacket.ResponseType checkAuthorized(final Credentials credentials) {
        if (Stream.of(RunBots.getDashboardConfig().accounts).anyMatch(acc -> Objects.equals(acc.username, credentials.username())
            && Objects.equals(acc.password, credentials.password()))) {
            return CheckAuthorizedPacket.ResponseType.AUTHORIZED;
        } else {
            return CheckAuthorizedPacket.ResponseType.DENIED;
        }
    }

    @Override
    public List<BotTypeEnum> getLoadedBotTypes() {
        return RunBots.getLoadedBots().stream()
            .map(b -> BotTypeEnum.byName(BotRegistry.getBotTypeName(b.getType())))
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public GenericResponse shutdownBot(final PacketContext context, final String botName) {
        final var botType = BotTypeEnum.byName(botName);
        if (botType != null) {
            if (RunBots.isBotLoaded(botType)) {
                log.warn("Shutting down bot {} at the request of {} via the dashboard!", botName,
                    DashboardSever.USERS.get(context.getSenderAddress()));
                RunBots.shutdownBot(botType);
                return GenericResponse.Type.SUCCESS.noMessage();
            } else {
                return GenericResponse.Type.INVALID_REQUEST.createF("Bot %s is not loaded!", botName);
            }
        } else {
            return GenericResponse.Type.INVALID_REQUEST.createF("Unknown bot type: %s", botName);
        }
    }
}