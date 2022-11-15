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
package com.mcmoddev.mmdbot.painter.util.dao;

import com.mcmoddev.mmdbot.core.database.jdbi.JdbiFactories;
import com.mcmoddev.mmdbot.painter.servericon.auto.AutomaticIconConfiguration;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterRowMapper(AutomaticIconConfiguration.RowMapper.class)
@RegisterRowMapper(AutoIconDAO.WithGuildConfiguration.RowMapper.class)
public interface AutoIconDAO extends Transactional<AutoIconDAO> {
    @SqlQuery("select * from auto_icons where guild_id = :guild")
    @Nullable AutomaticIconConfiguration get(@Bind("guild") ISnowflake guild);

    @SqlUpdate("insert or replace into auto_icons (guild_id, colours, log_channel, ring, enabled) values (:guild, :colours, :log_channel, :ring, :enabled)")
    void set(@Bind("guild") ISnowflake guild, @Bind("colours") String colours, @Bind("log_channel") long logChannel, @Bind("ring") boolean isRing, @Bind("enabled") boolean isEnabled);

    default void set(ISnowflake guild, AutomaticIconConfiguration configuration) {
        set(guild, configuration.serializeColors(), configuration.logChannelId(), configuration.isRing(), configuration.enabled());
    }

    @SqlUpdate("update or ignore auto_icons set enabled = :enabled where guild_id = :guild")
    void setEnabled(@Bind("guild") ISnowflake guild, @Bind("enabled") boolean enabled);

    @SqlUpdate("delete from auto_icons where guild_id = :guild")
    void delete(@Bind("guild") long guildId);

    @SqlQuery("select * from auto_icons where enabled = true")
    List<WithGuildConfiguration> allEnabled();

    record WithGuildConfiguration(long guildId, AutomaticIconConfiguration configuration) {
        public static final class RowMapper implements org.jdbi.v3.core.mapper.RowMapper<WithGuildConfiguration> {

            @Override
            public WithGuildConfiguration map(final ResultSet rs, final StatementContext ctx) throws SQLException {
                return new WithGuildConfiguration(
                    rs.getLong("guild_id"), AutomaticIconConfiguration.RowMapper.from(rs)
                );
            }
        }
    }
}
