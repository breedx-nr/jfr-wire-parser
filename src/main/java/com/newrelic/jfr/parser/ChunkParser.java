package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.Chunk;
import com.newrelic.jfr.model.ChunkHeader;

import java.nio.ByteBuffer;

public class ChunkParser {

    private ChunkHeaderParser chunkHeaderParser = new ChunkHeaderParser();

    public Chunk parse(byte[] chunk){
        ByteBuffer buff = ByteBuffer.wrap(chunk);
        ChunkHeader header = chunkHeaderParser.parse(buff);
        return null;
    }

}
