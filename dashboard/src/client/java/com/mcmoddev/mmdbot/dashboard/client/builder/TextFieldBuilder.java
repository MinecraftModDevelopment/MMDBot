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
package com.mcmoddev.mmdbot.dashboard.client.builder;

import com.mcmoddev.mmdbot.dashboard.client.builder.abstracts.RegionBuilder;
import com.mcmoddev.mmdbot.dashboard.client.util.ColourUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class TextFieldBuilder extends RegionBuilder<TextField, TextFieldBuilder> {
    public TextFieldBuilder(final TextField node) {
        super(node);
    }

    public TextFieldBuilder() {
        this(new TextField());
    }

    public TextFieldBuilder setOnKey(KeyActionType type, KeyCode keyCode, EventHandler<? super KeyEvent> action) {
        return doAndCast(f -> type.setAction(f, e -> {
            if (e.getCode() == keyCode) {
                action.handle(e);
            }
        }));
    }

    public TextFieldBuilder setOnEnter(EventHandler<ActionEvent> event) {
        return doAndCast(f -> f.setOnAction(event));
    }

    public TextFieldBuilder clickButtonOnEnter(Button button) {
        return doAndCast(f -> f.setOnAction(e -> button.getOnAction().handle(e)));
    }

    public TextFieldBuilder clickButtonOnEnter(Supplier<Button> button) {
        return doAndCast(f -> f.setOnAction(e -> button.get().getOnAction().handle(e)));
    }

    @Override
    public TextFieldBuilder setBackgroundColour(final Color colour) {
        return setInnerBackgroundColour(colour);
    }

    public TextFieldBuilder setInnerBackgroundColour(final Color colour) {
        return setStyle("-fx-control-inner-background: " + ColourUtils.toRGBAString(colour));
    }

    public enum KeyActionType {
        PRESSED((t, action) -> t.setOnKeyPressed(merge(t.getOnKeyPressed(), action))),
        RELEASED((t, action) -> t.setOnKeyReleased(merge(t.getOnKeyReleased(), action))),
        TYPED((t, action) -> t.setOnKeyTyped(merge(t.getOnKeyTyped(), action)));

        private final BiConsumer<TextField, EventHandler<? super KeyEvent>> setAction;

        KeyActionType(final BiConsumer<TextField, EventHandler<? super KeyEvent>> setAction) {
            this.setAction = setAction;
        }

        public void setAction(TextField field, EventHandler<? super KeyEvent> action) {
            setAction.accept(field, action);
        }

        private static <T extends Event> EventHandler<T> merge(@Nullable EventHandler<? super T> first, EventHandler<? super T> other) {
            if (first == null) {
                return other::handle;
            }
            return e -> {
                first.handle(e);
                other.handle(e);
            };
        }
    }
}