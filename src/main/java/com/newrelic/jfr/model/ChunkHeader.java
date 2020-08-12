package com.newrelic.jfr.model;


public class ChunkHeader {

    private final byte[] magic;
    private final short major;
    private final short minor;
    private final long chunkSize;
    private final long constantPoolOffset;
    private final long metadataOffset;
    private final long chunkStartNanos;
    private final long chunkDurationNanos;
    private final long chunkStartTicks;
    private final long clockFrequency;

    private ChunkHeader(Builder builder) {
        this.magic = builder.magic;
        this.major = builder.major;
        this.minor = builder.minor;
        this.chunkSize = builder.chunkSize;
        this.constantPoolOffset = builder.constantPoolOffset;
        this.metadataOffset = builder.metadataOffset;
        this.chunkStartNanos = builder.chunkStartNanos;
        this.chunkDurationNanos = builder.chunkDurationNanos;
        this.chunkStartTicks = builder.chunkStartTicks;
        this.clockFrequency = builder.clockFrequency;
    }

    public static Builder builder() {
        return new Builder();
    }

    public byte[] getMagic() {
        return magic;
    }

    public short getMajor() {
        return major;
    }

    public short getMinor() {
        return minor;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public long getConstantPoolOffset() {
        return constantPoolOffset;
    }

    public long getMetadataOffset() {
        return metadataOffset;
    }

    public long getChunkStartNanos() {
        return chunkStartNanos;
    }

    public long getChunkDurationNanos() {
        return chunkDurationNanos;
    }

    public long getChunkStartTicks() {
        return chunkStartTicks;
    }

    public long getClockFrequency() {
        return clockFrequency;
    }

    public static class Builder {
        byte[] magic;
        short major;
        short minor;
        long chunkSize;
        long constantPoolOffset;
        long metadataOffset;
        long chunkStartNanos;
        long chunkDurationNanos;
        long chunkStartTicks;
        long clockFrequency;

        public Builder magic(byte[] magic) {
            this.magic = magic;
            return this;
        }

        public Builder major(short major) {
            this.major = major;
            return this;
        }

        public Builder minor(short minor) {
            this.minor = minor;
            return this;
        }

        public Builder chunkSize(long chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder checkpointOffset(long checkpointOffset){
            this.constantPoolOffset = checkpointOffset;
            return this;
        }

        public Builder metadataOffset(long metadataOffset){
            this.metadataOffset = metadataOffset;
            return this;
        }

        public Builder chunkStartNanos(long chunkStartNanos){
            this.chunkStartNanos = chunkStartNanos;
            return this;
        }

        public Builder chunkDurationNanos(long chunkDurationNanos){
            this.chunkDurationNanos = chunkDurationNanos;
            return this;
        }

        public Builder chunkStartTicks(long chunkStartTicks){
            this.chunkStartTicks = chunkStartTicks;
            return this;
        }

        public Builder clockFrequency(long clockFrequency){
            this.clockFrequency = clockFrequency;
            return this;
        }

        public ChunkHeader build() {
            return new ChunkHeader(this);
        }
    }
}
