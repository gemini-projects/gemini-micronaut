package it.at7.gemini.micronaut.core;

import java.util.Optional;

public class DataResult<T> implements CommonResult {
    private long lastUpdateTime;
    private T data;

    public DataResult(T data) {
        this.data = data;
    }

    public static <T> DataResult<T> from(T data) {
        return new DataResult<T>(data);
    }

    public T getData() {
        return data;
    }

    public Optional<Long> getLastUpdateTime() {
        return lastUpdateTime == -1 ? Optional.empty() : Optional.of(lastUpdateTime);
    }

    public DataResult<T> setLastUpdate(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

}
