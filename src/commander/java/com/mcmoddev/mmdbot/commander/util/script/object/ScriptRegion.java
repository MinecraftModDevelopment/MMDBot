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
package com.mcmoddev.mmdbot.commander.util.script.object;

import com.mcmoddev.mmdbot.core.annotation.ExposeScripting;
import net.dv8tion.jda.api.Region;

public final class ScriptRegion {

    @ExposeScripting
    public final String key;
    @ExposeScripting
    public final String name;
    @ExposeScripting
    public final String emoji;

    private final boolean isVip;

    public ScriptRegion(Region region) {
        this.key = region.getKey();
        this.name = region.getName();
        this.emoji = region.getEmoji();
        this.isVip = region.isVip();
    }

    @ExposeScripting
    public boolean isVip() {
        return isVip;
    }

    @Override
    public String toString() {
        return name;
    }
}
