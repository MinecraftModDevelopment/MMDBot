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
package com.mcmoddev.mmdbot.core.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EmptyRestAction<T> implements RestAction<T> {

    /**
     * Common instance for {@code empty()}.
     */
    private static final EmptyRestAction<?> EMPTY = new EmptyRestAction<>();

    public static<T> RestAction<T> empty() {
        @SuppressWarnings("unchecked")
        RestAction<T> t = (EmptyRestAction<T>) EMPTY;
        return t;
    }

    private EmptyRestAction() {

    }

    @NotNull
    @Override
    public JDA getJDA() {
        return null;
    }

    @NotNull
    @Override
    public RestAction<T> setCheck(@Nullable final BooleanSupplier checks) {
        return this;
    }

    @Override
    public void queue(@Nullable final Consumer<? super T> success, @Nullable final Consumer<? super Throwable> failure) {

    }

    @Override
    public T complete(final boolean shouldQueue) {
        return null;
    }

    @NotNull
    @Override
    public CompletableFuture<T> submit(final boolean shouldQueue) {
        return CompletableFuture.failedFuture(new IllegalArgumentException());
    }
}
