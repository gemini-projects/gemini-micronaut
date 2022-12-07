package it.at7.gemini.micronaut.core;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TimeWatchLogger {
    private final static long ID_MAX = 10000000L;
    private final static Map<String, AtomicLong> logIDS = new ConcurrentHashMap<>();

    private final Logger logger;
    private final String tag;
    private final long logID;

    private final long start;

    public TimeWatchLogger(Logger logger, String tag) {
        this.logger = logger;
        this.tag = tag;
        this.start = System.currentTimeMillis();
        this.logID = logIDS.compute(logger.getName(), (s, atomicLong) -> {
            if (atomicLong == null || atomicLong.get() == ID_MAX)
                return new AtomicLong(1);
            return atomicLong;
        }).getAndIncrement();
    }

    public long info(String message) {
        long current = System.currentTimeMillis() - start;
        this.logger.info("[{}-{} {}] {}", tag, logID, current, message);
        return current;
    }

    public long debug(String message) {
        long current = System.currentTimeMillis() - start;
        this.logger.debug("[{}-{} {}] {}", tag, logID, current, message);
        return current;
    }

    public long warn(String message) {
        long current = System.currentTimeMillis() - start;
        this.logger.warn("[{}-{} {}] {}", tag, logID, current, message);
        return current;
    }

    public long warn(String message, Exception e) {
        long current = System.currentTimeMillis() - start;
        this.logger.warn("[{}-{} {}] {}", tag, logID, current, message, e);
        return current;
    }

    public long getStart() {
        return start;
    }

    public static TimeWatchLogger info(Logger logger, String tag, String message) {
        TimeWatchLogger timeWatchLogger = new TimeWatchLogger(logger, tag);
        timeWatchLogger.info(message);
        return timeWatchLogger;
    }

    public static TimeWatchLogger debug(Logger logger, String tag, String message) {
        TimeWatchLogger timeWatchLogger = new TimeWatchLogger(logger, tag);
        timeWatchLogger.debug(message);
        return timeWatchLogger;
    }

    public long error(String message, Throwable e) {
        long current = System.currentTimeMillis() - start;
        this.logger.error("[{}-{} {}] {}", tag, logID, current, message, e);
        return current;
    }

    public long error(String message) {
        long current = System.currentTimeMillis() - start;
        this.logger.error("[{}-{} {}] {}", tag, logID, current, message);
        return current;
    }
}
