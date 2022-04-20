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
package com.mcmoddev.mmdbot.commander.custompings;

import com.mcmoddev.mmdbot.core.dfu.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public record CustomPing(Pattern pattern, String text) implements Predicate<Message> {

    /**
     * The codec used for serializing custom pings.
     */
    public static final Codec<CustomPing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codecs.PATTERN.fieldOf("pattern").forGetter(CustomPing::pattern),
        Codec.STRING.optionalFieldOf("text", "").forGetter(CustomPing::text)
    ).apply(instance, CustomPing::new));

    @Override
    public boolean test(final Message message) {
        return pattern.asMatchPredicate().test(message.getContentRaw());
    }
}
