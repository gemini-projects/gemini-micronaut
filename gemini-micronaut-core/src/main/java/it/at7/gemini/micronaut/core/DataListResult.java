package it.at7.gemini.micronaut.core;

import java.util.List;
import java.util.Optional;

public class DataListResult<T> implements CommonResult {
    private final List<T> data;
    private long lastUpdateTime = -1;

    public DataListResult(List<T> data) {
        this.data = data;
    }

    public static <T> DataListResult<T> from(List<T> data) {
        return new DataListResult<T>(data);
    }

    public List<T> getData() {
        return data;
    }

    public Optional<Long> getLastUpdateTime() {
        return lastUpdateTime == -1 ? Optional.empty() : Optional.of(lastUpdateTime);
    }

    public DataListResult<T> setLastUpdate(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }
}
