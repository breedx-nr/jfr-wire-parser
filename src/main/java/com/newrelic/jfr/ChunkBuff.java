package com.newrelic.jfr;

import java.nio.ByteBuffer;

/**
 * Buffers chunks until a whole chunk is available.
 */
public class ChunkBuff {

    public static final int CAPACITY = 25 * 1024 * 1024;
    public static final int UNKNOWN_SIZE = Integer.MAX_VALUE;
    private final ByteBuffer buff = ByteBuffer.allocate(CAPACITY);

    // Adds a block of bytes to this buffer.
    public void add(byte[] block) {
        if (!complete()) {
            buff.put(block);
        }
    }

    // gets the chunk, or null if not big enough yet
    public byte[] get() {
        long unreasonableSize = targetSize();
        int size = (int) unreasonableSize;
        if (size == UNKNOWN_SIZE) {
            return null;
        }
        byte[] dst = new byte[size];
        int previousPosition = buff.position();
        buff.position(0);
        buff.get(dst, 0, size);
        buff.position(previousPosition);
        return dst;
    }

    public boolean complete() {
        return buff.position() >= targetSize();
    }

    private long targetSize() {
        if (buff.position() < 16) {
            return UNKNOWN_SIZE;
        }
        return buff.getLong(8);
    }
}
