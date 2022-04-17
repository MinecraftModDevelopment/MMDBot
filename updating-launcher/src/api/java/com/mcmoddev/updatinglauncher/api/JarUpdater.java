package com.mcmoddev.updatinglauncher.api;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;

/**
 * An updater which manages and updates a jar.
 */
public interface JarUpdater extends Runnable {

    /**
     * Starts a process.
     */
    void startProcess();

    /**
     * Kills the currently running process and updates it.
     * @param release the release to update to
     */
    void killAndUpdate(final Release release) throws Exception;

    /**
     * Tries to start the jar, after the launcher was started.
     */
    void tryFirstStart();

    /**
     * Clears the currently running process.
     */
    void clearProcess();

    /**
     * @return the update checker
     */
    UpdateChecker getUpdateChecker();

    /**
     * @return the path of the managed jar
     */
    Path getJarPath();

    /**
     * @return an optional which may contain the version of the current jar
     */
    Optional<String> getJarVersion();

    /**
     * @return the currently running process, or else null
     */
    @Nullable
    ProcessInfo getProcess();

    /**
     * Runs this updater.
     */
    @Override
    void run();
}
