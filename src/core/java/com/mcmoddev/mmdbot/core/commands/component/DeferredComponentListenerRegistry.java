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
package com.mcmoddev.mmdbot.core.commands.component;

import com.mcmoddev.mmdbot.core.commands.component.storage.ComponentStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeferredComponentListenerRegistry {
    private ComponentManager manager;
    private final List<ComponentListener> deferredListeners = Collections.synchronizedList(new ArrayList<>());

    public ComponentManager createManager(final ComponentStorage storage) {
        this.manager = new ComponentManager(storage, deferredListeners);
        return manager;
    }

    public ComponentListener.Builder createListener(final String featureId) {
        return ComponentListener.builder(featureId, listener -> {
            if (manager == null) {
                deferredListeners.add(listener);
            } else {
                manager.addListener(listener);
            }
        });
    }
}
