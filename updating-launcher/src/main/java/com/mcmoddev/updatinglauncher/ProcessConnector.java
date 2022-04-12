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
package com.mcmoddev.updatinglauncher;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProcessConnector extends Remote {

    int PORT = 6291;
    String NAME_PROPERTY = "com.mcmodedev.updatinglauncher.connector.name";
    String BASE_NAME = "ULProcessConnector";

    ThreadInfo[] getThreads() throws RemoteException;
    double getCPULoad() throws RemoteException;
    MemoryUsage getMemoryUsage() throws RemoteException;
}
