package com.newrelic.jfr.model;

public class PoolDescriptor {
    private final int offset;
    private final int size;
    private final int typeId;
    private final long timestamp;
    private final long duration;
    private final long offsetToNextPool;
    private final boolean flush;

    public PoolDescriptor(int offset, int size, int typeId, long timestamp, long duration, long offsetToNextPool, boolean flush) {
        this.offset = offset;
        this.size = size;
        this.typeId = typeId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.offsetToNextPool = offsetToNextPool;
        this.flush = flush;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public int getTypeId() {
        return typeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public long getOffsetToNextPool() {
        return offsetToNextPool;
    }

    public boolean isFlush() {
        return flush;
    }
}
