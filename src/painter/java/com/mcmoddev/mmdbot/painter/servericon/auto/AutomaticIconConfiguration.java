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
package com.mcmoddev.mmdbot.painter.servericon.auto;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmoddev.mmdbot.painter.servericon.IconConfiguration;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconMaker;
import org.jdbi.v3.core.statement.StatementContext;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public record AutomaticIconConfiguration(
    List<Integer> colours, long logChannelId, boolean isRing, boolean enabled
) {
    public static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping().create();

    public String serializeColors() {
        return GSON.toJson(colours());
    }

    public BufferedImage createImage(int day) throws IOException {
        return ServerIconMaker.createIcon(IconConfiguration.builder()
            .setCircular(isRing).setHasRing(isRing)
            .setColour(colours.get(day - 1))
            .build());
    }

    public static final class RowMapper implements org.jdbi.v3.core.mapper.RowMapper<AutomaticIconConfiguration> {
        private static final Type COLOUR_LIST_TYPE = new TypeToken<List<Integer>>() {}.getType();

        @Override
        public AutomaticIconConfiguration map(final ResultSet rs, final StatementContext ctx) throws SQLException {
            return from(rs);
        }

        public static AutomaticIconConfiguration from(final ResultSet rs) throws SQLException {
            return new AutomaticIconConfiguration(
                GSON.fromJson(rs.getString("colours"), COLOUR_LIST_TYPE),
                rs.getLong("log_channel"),
                rs.getBoolean("ring"), rs.getBoolean("enabled")
            );
        }
    }
}
