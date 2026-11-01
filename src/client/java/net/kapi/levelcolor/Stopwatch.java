package net.kapi.levelcolor;

import java.util.Locale;

public final class Stopwatch {
    private static long accumulatedNanos;
    private static long startedAtNanos;
    private static boolean running;

    private Stopwatch() {}

    public static synchronized void start() {
        accumulatedNanos = 0L;
        startedAtNanos = System.nanoTime();
        running = true;
    }

    public static synchronized void stop() {
        if (!running) return;
        accumulatedNanos += System.nanoTime() - startedAtNanos;
        running = false;
    }

    public static synchronized void resume() {
        if (running) return;
        startedAtNanos = System.nanoTime();
        running = true;
    }

    public static synchronized void reset() {
        accumulatedNanos = 0L;
        startedAtNanos = System.nanoTime();
        running = false;
    }

    public static synchronized boolean isRunning() { return running; }

    public static synchronized String formattedTime() {
        long nanos = accumulatedNanos + (running ? System.nanoTime() - startedAtNanos : 0L);
        long totalSeconds = nanos / 1_000_000_000L;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds / 60) % 60;
        long seconds = totalSeconds % 60;
        long centiseconds = (nanos / 10_000_000L) % 100;
        return String.format(Locale.ROOT, "%02d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
    }
}
