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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DelegatedProcess extends Process {

    private final Process delegate;

    public DelegatedProcess(final Process delegate) {
        this.delegate = delegate;
    }

    @Override
    public OutputStream getOutputStream() {
        return delegate.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return delegate.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return delegate.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return delegate.waitFor();
    }

    @Override
    public boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
        return delegate.waitFor(timeout, unit);
    }

    @Override
    public int exitValue() {
        return delegate.exitValue();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public Process destroyForcibly() {
        return delegate.destroyForcibly();
    }

    @Override
    public boolean supportsNormalTermination() {
        return delegate.supportsNormalTermination();
    }

    @Override
    public boolean isAlive() {
        return delegate.isAlive();
    }

    @Override
    public long pid() {
        return delegate.pid();
    }

    @Override
    public CompletableFuture<Process> onExit() {
        return delegate.onExit();
    }

    @Override
    public ProcessHandle toHandle() {
        return delegate.toHandle();
    }

    @Override
    public ProcessHandle.Info info() {
        return delegate.info();
    }

    @Override
    public Stream<ProcessHandle> children() {
        return delegate.children();
    }

    @Override
    public Stream<ProcessHandle> descendants() {
        return delegate.descendants();
    }

}
