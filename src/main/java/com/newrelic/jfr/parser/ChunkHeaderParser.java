package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkHeader;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;

public class ChunkHeaderParser {

    static final byte[] EXPECTED_MAGIC = new byte[]{'F', 'L', 'R', 0};
    public static final int EXPECTED_MAJOR_VERSION = 2;
    public final static int EVENT_START_OFFSET = 68;

    public ChunkHeader parse(ByteBuffer buff) {
        byte[] magic = readMagic(buff);
        if (!Arrays.equals(magic, EXPECTED_MAGIC)) {
            throw new JfrParseException("Unexpected start of chunk: " + magic + " expecgted " + EXPECTED_MAGIC);
        }
        short major = readMajor(buff);
        if (major != EXPECTED_MAJOR_VERSION) {
            throw new JfrParseException("Unexpected major version " + major + " expected " + EXPECTED_MAJOR_VERSION);
        }

        return ChunkHeader.builder()
                .magic(magic)
                .major(major)
                .minor(readMinor(buff))
                .chunkSize(readChunkSize(buff))
                .checkpointOffset(readCheckpointOffset(buff))
                .metadataOffset(readMetadataOffset(buff))
                .chunkStartNanos(readStartNanos(buff))
                .chunkDurationNanos(readChunkDurationNanos(buff))
                .chunkStartTicks(readChunkStartTicks(buff))
                .clockFrequency(readClockFreq(buff))
                .build();

    }

    private byte[] readMagic(ByteBuffer buff) {
        byte[] dst = new byte[4];
        buff.get(dst, 0, 4);
        return dst;
    }

    private short readMajor(ByteBuffer buff) {
        return buff.getShort(4);
    }

    private short readMinor(ByteBuffer buff) {
        return buff.getShort(6);
    }

    private long readChunkSize(ByteBuffer buff) {
        return buff.getLong(8);
    }

    private long readCheckpointOffset(ByteBuffer buff) {
        return buff.getLong(16);
    }

    private long readMetadataOffset(ByteBuffer buff) {
        return buff.getLong(24);
    }

    private long readStartNanos(ByteBuffer buff) {
        return buff.getLong(32);
    }

    private long readChunkDurationNanos(ByteBuffer buff) {
        return buff.getLong(40);
    }

    private long readChunkStartTicks(ByteBuffer buff) {
        return buff.getLong(48);
    }

    private long readClockFreq(ByteBuffer buff) {
        return buff.getLong(56);
    }
}
