package com.mcmoddev.updatinglauncher.api;

import com.mcmoddev.updatinglauncher.api.connector.ProcessConnector;
import org.jetbrains.annotations.Nullable;

public interface ProcessInfo {

    Process process();

    @Nullable
    Release release();

    @Nullable
    ProcessConnector connector();
}
