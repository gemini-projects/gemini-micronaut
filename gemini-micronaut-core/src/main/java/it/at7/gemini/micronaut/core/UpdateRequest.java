package it.at7.gemini.micronaut.core;

public class UpdateRequest {
    public static final UpdateRequest STANDARD = new UpdateRequest(UpdateStrategy.FULL);
    private final UpdateStrategy updateStrategy;

    public UpdateRequest(UpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UpdateStrategy updateStrategy;

        public Builder() {
            this.updateStrategy = UpdateStrategy.FULL;
        }

        public Builder setUpdateStrategy(UpdateStrategy updateStrategy) {
            this.updateStrategy = updateStrategy;
            return this;
        }

        public UpdateRequest build() {
            return new UpdateRequest(this.updateStrategy);
        }
    }
}
