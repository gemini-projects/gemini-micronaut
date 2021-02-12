package it.at7.gemini.micronaut.core;

public class EntityTimes {

    private Long lastCreateTimeUnix;
    private Long lastUpdateTimeUnix;
    private Long lastDeleteTimeUnix;

    public EntityTimes(Long lastCreateTimeUnix, Long lastUpdateTimeUnix, Long lastDeleteTimeUnix) {
        this.lastCreateTimeUnix = lastCreateTimeUnix;
        this.lastUpdateTimeUnix = lastUpdateTimeUnix;
        this.lastDeleteTimeUnix = lastDeleteTimeUnix;
    }

    public Long getLastCreateTimeUnix() {
        return lastCreateTimeUnix;
    }

    public Long getLastUpdateTimeUnix() {
        return lastUpdateTimeUnix;
    }

    public Long getLastDeleteTimeUnix() {
        return lastDeleteTimeUnix;
    }
}
