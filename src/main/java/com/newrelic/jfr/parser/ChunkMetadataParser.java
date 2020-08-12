package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkHeader;
import com.newrelic.jfr.model.ChunkMetadata;

import java.nio.ByteBuffer;

public class ChunkMetadataParser {

    private final ByteSupport bs = new ByteSupport();

    public ChunkMetadata parse(ByteBuffer buff, ChunkHeader header) {
        buff.position((int) header.getMetadataOffset());
        long size = bs.readVarLen(buff);
        return null;
    }
}
