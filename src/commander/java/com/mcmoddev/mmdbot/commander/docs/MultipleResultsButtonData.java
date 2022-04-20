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
package com.mcmoddev.mmdbot.commander.docs;

import de.ialistannen.javadocapi.querying.FuzzyQueryResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record MultipleResultsButtonData(long userId, List<String> queries) {

    public MultipleResultsButtonData(long userId, Collection<FuzzyQueryResult> results) {
        this(userId, results.stream().map(r -> r.getQualifiedName().asString()).collect(Collectors.toList()));
    }

    public static MultipleResultsButtonData fromArguments(final List<String> args) {
        return new MultipleResultsButtonData(Long.parseLong(args.get(0)), args.subList(1, args.size()));
    }

    public List<String> toArguments() {
        if (queries instanceof ArrayList<String>) {
            queries.add(0, String.valueOf(userId));
            return queries;
        }
        final var copy = new ArrayList<>(queries);
        copy.add(0, String.valueOf(userId));
        return copy;
    }

}
