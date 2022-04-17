package com.mcmoddev.updatinglauncher.api;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface UpdateChecker {

    /**
     * Resolves the latest release.
     *
     * @return the latest release
     */
    @Nullable
    Release resolveLatestRelease() throws IOException, InterruptedException;

    /**
     * Finds a new release.
     *
     * @return if a new release has been found
     */
    boolean findNew() throws IOException, InterruptedException;

    /**
     * Gets the latest found release. This might not always be up-to-date.
     *
     * @return the latest found release
     */
    @Nullable
    Release getLatestFound();

    /**
     * Gets a release by a tag name.
     * @param tagName the tag name of the release to get
     * @return the release, if found or else null
     */
    @Nullable
    Release getReleaseByTagName(String tagName) throws IOException, InterruptedException;
}
