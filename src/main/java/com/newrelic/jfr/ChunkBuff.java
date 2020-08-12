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
        int size = targetSize();
        if (size == UNKNOWN_SIZE) {

        }
        byte[] dst = new byte[size];
        buff.get(dst, 0, size);
        return dst;
    }

    private boolean complete() {
        return buff.position() >= targetSize();
    }

    private int targetSize() {
        if (buff.position() < 16) {
            return UNKNOWN_SIZE;
        }
        byte[] dst = new byte[8];
        buff.get(dst, 8, 8);
        return (int) ByteBuffer.wrap(dst).getLong();
    }
}
