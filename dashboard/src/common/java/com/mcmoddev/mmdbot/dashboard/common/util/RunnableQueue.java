package com.mcmoddev.mmdbot.dashboard.common.util;

import java.util.LinkedList;
import java.util.function.Consumer;

public final class RunnableQueue<T> implements Runnable {

    private final Consumer<T> runner;
    private final LinkedList<T> queue = new LinkedList<>();

    private RunnableQueue(final Consumer<T> runner) {
        this.runner = runner;
    }

    public static <T> RunnableQueue<T> create(Consumer<T> runner) {
        return new RunnableQueue<>(runner);
    }

    public static RunnableQueue<Runnable> createRunnable() {
        return new RunnableQueue<>(Runnable::run);
    }

    public RunnableQueue<T> addFirst(T toRun) {
        queue.addFirst(toRun);
        return this;
    }

    public RunnableQueue<T> addLast(T toRun) {
        queue.addLast(toRun);
        return this;
    }

    @Override
    public void run() {
        do {
            runner.accept(queue.poll());
        } while (!queue.isEmpty());
    }
}
