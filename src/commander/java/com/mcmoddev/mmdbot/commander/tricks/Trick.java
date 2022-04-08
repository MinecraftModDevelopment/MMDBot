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
package com.mcmoddev.mmdbot.commander.tricks;

import com.mojang.serialization.Codec;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;

/**
 * The interface Trick.
 *
 * @author williambl
 */
public interface Trick {

    /**
     * Gets names.
     *
     * @return Get trick names.
     */
    List<String> getNames();

    /**
     * Executes the trick
     *
     * @param context the context to execute the message in
     */
    void execute(TrickContext context);

    /**
     * @return the raw representation of this trick
     */
    String getRaw();

    /**
     * @return the type of the trick
     */
    TrickType<?> getType();

    default Codec<?> getCodec() {
        return getType().getCodec();
    }

    /**
     * The TrickType interface. Every trick requires a trick type to be registered
     *
     * @param <T> the trick
     */
    interface TrickType<T extends Trick> {

        /**
         * Gets the trick class.
         *
         * @return the trick class
         */
        Class<T> getClazz();

        /**
         * Gets argument names for creating from command.
         *
         * @return arg names
         */
        List<String> getArgNames();

        /**
         * Create a trick from string arguments.
         *
         * @param args the args as a single string
         * @return the trick
         */
        T createFromArgs(String args);

        /**
         * Gets the arguments that will be sent in a modal.
         *
         * @return the arguments
         */
        List<ActionRow> getModalArguments();

        /**
         * Creates the trick from a modal.
         *
         * @param modal the modal event
         * @return the trick
         */
        T createFromModal(ModalInteractionEvent modal);

        /**
         * Gets the codec used for deserializing and serializing the trick.
         * @return the codec
         */
        Codec<T> getCodec();
    }
}
