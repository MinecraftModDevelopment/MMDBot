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
package com.mcmoddev.mmdbot.commander.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.matyrobbrt.curseforgeapi.util.gson.RecordTypeAdapterFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface StringSerializer<T> {
    Gson RECORD_GSON = new GsonBuilder().registerTypeAdapterFactory(new RecordTypeAdapterFactory()).create();
    StringSerializer<String> SELF = new StringSerializer<>() {
        @NotNull
        @Override
        public String serialize(final String input) {
            return input;
        }

        @NotNull
        @Override
        public String deserialize(final String input) {
            return input;
        }
    };

    @Nonnull
    String serialize(T input);

    @Nonnull
    T deserialize(String input);

    static <T> StringSerializer<T> json(Gson gson, Class<T> type) {
        return new StringSerializer<>() {
            @NotNull
            @Override
            public String serialize(final T input) {
                return gson.toJson(input, type);
            }

            @NotNull
            @Override
            public T deserialize(final String input) {
                return gson.fromJson(input, type);
            }
        };
    }
}
