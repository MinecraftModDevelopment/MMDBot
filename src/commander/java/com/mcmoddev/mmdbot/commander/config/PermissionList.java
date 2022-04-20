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
package com.mcmoddev.mmdbot.commander.config;

import io.leangen.geantyref.TypeToken;
import lombok.experimental.Delegate;
import net.dv8tion.jda.api.Permission;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PermissionList implements Set<Permission> {
    @Delegate
    private final Set<Permission> delegate;

    public PermissionList(final Set<Permission> delegate) {
        this.delegate = delegate;
    }

    public PermissionList(long permissionRaw) {
        this(Permission.getPermissions(permissionRaw));
    }

    public static final class Serializer implements TypeSerializer<PermissionList> {

        @Override
        public PermissionList deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
            if (node.empty() || node.isNull()) {
                return null;
            }
            if (node.raw() instanceof Number num) {
                return new PermissionList(num.longValue());
            }
            final var list = node.get(new TypeToken<List<String>>() {});
            if (list == null) return null;
            final EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
            outer:
            for (final String perm : list) {
                for (final Permission permission : Permission.values()) {
                    if (permission.getName().equals(perm) || permission.name().equals(perm)) {
                        permissions.add(permission);
                        continue outer;
                    }
                }
            }
            return new PermissionList(permissions);
        }

        @Override
        public void serialize(final Type type, @Nullable final PermissionList obj, final ConfigurationNode node) throws SerializationException {
            if (obj == null) {
                node.raw(null);
                return;
            }
            node.set(new TypeToken<List<String>>() {}.getType(), obj.delegate.stream().map(Permission::getName).toList());
        }
    }
}
