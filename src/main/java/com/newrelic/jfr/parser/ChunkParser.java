package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.Chunk;
import com.newrelic.jfr.model.ChunkHeader;
import com.newrelic.jfr.model.ChunkMetadata;

import java.nio.ByteBuffer;

public class ChunkParser {

    private ChunkHeaderParser headerParser = new ChunkHeaderParser();
    private ChunkMetadataParser metadataParser = new ChunkMetadataParser();

    public Chunk parse(byte[] chunk){
        var buff = ByteBuffer.wrap(chunk);
        var header = headerParser.parse(buff);
        ChunkMetadata meta = metadataParser.parse(buff, header);
        return null;
    }

}
