package it.at7.gemini.micronaut.core;

import java.util.Optional;

public class DataCountResult implements CommonResult {
    private long lastUpdateTime = -1;
    private long estimateCount = -1;
    private long count;

    private DataCountResult() {
    }

    public static DataCountResult fromCount(long count) {
        DataCountResult dataCountResult = new DataCountResult();
        dataCountResult.count = count;
        return dataCountResult;
    }

    public static DataCountResult fromEstimateCount(long estimateCount) {
        DataCountResult dataCountResult = new DataCountResult();
        dataCountResult.estimateCount = estimateCount;
        return dataCountResult;
    }

    public Optional<Long> getEstimateCount() {
        return estimateCount == -1 ? Optional.empty() : Optional.of(estimateCount);
    }

    public Optional<Long> getCount() {
        return count == -1 ? Optional.empty() : Optional.of(count);
    }

    @Override
    public Optional<Long> getLastUpdateTime() {
        return lastUpdateTime == -1 ? Optional.empty() : Optional.of(lastUpdateTime);
    }

    public DataCountResult setLastUpdate(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public DataCountResult setEstimateCount(long estimateCount) {
        this.estimateCount = estimateCount;
        return this;
    }

    public DataCountResult setCount(long count) {
        this.count = count;
        return this;
    }

}
