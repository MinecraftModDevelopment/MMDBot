package com.mcmoddev.mmdbot.commander.util;

import com.mcmoddev.mmdbot.core.util.ThreadedEventListener;
import com.mcmoddev.mmdbot.core.util.Utils;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@UtilityClass
public class EventListeners {

    public static final ThreadedEventListener COMMANDS_LISTENER;

    static {
        final var mainGroup = new ThreadGroup("The Commander");

        // Commands
        {
            final var group = new ThreadGroup("Command Listeners");
            final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r ->
                Utils.setThreadDaemon(new Thread(group, r, "CommandListener #%s".formatted(group.activeCount())),
                    true));
            poll.setKeepAliveTime(30, TimeUnit.MINUTES);
            poll.allowCoreThreadTimeOut(true);
            COMMANDS_LISTENER = new ThreadedEventListener(poll);
        }
    }

    public static void register(Consumer<EventListener> registerer) {
        registerer.accept(COMMANDS_LISTENER);
    }

    public static void clear() {
        COMMANDS_LISTENER.clear();
    }
}
