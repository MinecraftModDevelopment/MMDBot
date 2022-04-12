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
package com.mcmoddev.updatinglauncher;

import java.lang.instrument.Instrumentation;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Agent {
    // Keep a "strong" reference to the connector and its registry, to avoid GC picking it up
    private static Registry registry;
    private static ProcessConnector server;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.err.println("Starting RMI on port " + ProcessConnector.PORT);
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            registry = LocateRegistry.createRegistry(ProcessConnector.PORT);
            server = new ProcessConnectorServer();

            final ProcessConnector stub = (ProcessConnector) UnicastRemoteObject.exportObject(server, ProcessConnector.PORT);
            registry.rebind(ProcessConnector.NAME, stub);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    registry.unbind(ProcessConnector.NAME);
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }));
            System.err.println("Process Connector ready!");
        } catch (Exception e) {
            System.out.println("Exception starting RMI server: " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
