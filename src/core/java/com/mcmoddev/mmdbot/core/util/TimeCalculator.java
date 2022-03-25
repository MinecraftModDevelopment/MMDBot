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

public class TimeCalculator {
    private long seconds;

    public TimeCalculator addSeconds(long seconds) {
        this.seconds += seconds;
        return this;
    }

    public TimeCalculator addMinutes(long minutes) {
        this.seconds += minutes * 60;
        return this;
    }

    public TimeCalculator addHours(long hours) {
        this.seconds += hours * 60 * 60;
        return this;
    }

    public TimeCalculator addDays(long days) {
        this.seconds += days * 60 * 60 * 24;
        return this;
    }

    public TimeCalculator addYears(long years) {
        this.seconds += years * 60 * 60 * 24 * 365;
        return this;
    }

    public long toSeconds() {
        return seconds;
    }
}
