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
package com.mcmoddev.mmdbot.commander.docs;

import java.util.List;

public record DocsButtonData(long buttonOwner, String query, boolean shortDescription, boolean omitTags) {

    public static DocsButtonData fromArguments(final List<String> args) {
        return new DocsButtonData(
            Long.parseLong(args.get(0)),
            args.get(1),
            Boolean.parseBoolean(args.get(2)),
            Boolean.parseBoolean(args.get(3))
        );
    }

    public List<String> toArguments() {
        return List.of(
            String.valueOf(buttonOwner),
            query,
            String.valueOf(shortDescription),
            String.valueOf(omitTags)
        );
    }

    public DocsButtonData withShortDescription(final boolean shortDescription) {
        return new DocsButtonData(buttonOwner(), query(), shortDescription, omitTags());
    }

    public DocsButtonData withOmitTags(final boolean omitTags) {
        return new DocsButtonData(buttonOwner(), query(), shortDescription(), omitTags);
    }
}
