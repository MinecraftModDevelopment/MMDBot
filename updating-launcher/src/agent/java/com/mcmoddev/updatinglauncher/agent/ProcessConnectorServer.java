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
package com.mcmoddev.updatinglauncher.agent;

import com.mcmoddev.updatinglauncher.api.connector.MemoryUsage;
import com.mcmoddev.updatinglauncher.api.connector.ProcessConnector;
import com.mcmoddev.updatinglauncher.api.Properties;
import com.mcmoddev.updatinglauncher.api.connector.ThreadInfo;
import com.mcmoddev.updatinglauncher.api.StatusListener;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;

public class ProcessConnectorServer implements ProcessConnector {
    @Override
    public ThreadInfo[] getThreads() throws RemoteException {
        return Thread.getAllStackTraces()
            .entrySet()
            .stream()
            .map(e -> ThreadInfo.fromThread(e.getKey(), e.getValue())).toArray(ThreadInfo[]::new);
    }

    @Override
    public double getCPULoad() throws RemoteException {
        return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getCpuLoad();
    }

    @Override
    public MemoryUsage getMemoryUsage() throws RemoteException {
        final Runtime runtime = Runtime.getRuntime();
        return new MemoryUsage(runtime.totalMemory(), runtime.freeMemory());
    }

    @Override
    public HashMap<String, Object> getProcessInfoProfiling() throws RemoteException {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("agentVersion", Agent.VERSION);

        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        final ProcessHandle handle = ProcessHandle.current();

        // App info
        map.put("name", runtimeBean.getName());
        map.put("epochMillis", System.currentTimeMillis());
        map.put("pid", handle.pid());
        try {
            map.put("host", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            map.put("host", "unknown");
        }
        final String jar = System.getProperty(Properties.JAR_PATH);
        map.put("appJar", jar);
        try {
            map.put("appClass", new JarFile(jar).getManifest().getMainAttributes().getValue("Main-Class"));
        } catch (Exception e) {
            map.put("appClass", "unknown");
        }

        map.put("jvmInputArguments", runtimeBean.getInputArguments() == null ? List.of() : new ArrayList<>(runtimeBean.getInputArguments()));
        map.put("xmxBytes", ProfilingUtils.getJvmXmxBytes(runtimeBean.getInputArguments()));
        map.put("command", handle.info().command().orElse(""));
        map.put("commandLine", handle.info().commandLine().orElse(""));
        map.put("start", handle.info().startInstant().map(Instant::toString).orElse("unknown"));
        map.put("classpath", runtimeBean.getClassPath());

        return map;
    }

    @Override
    public void onShutdown() throws RemoteException {
        Agent.executeOnListeners(StatusListener::onShutdown);
    }
}
