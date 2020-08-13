package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.Chunk;
import com.newrelic.jfr.model.ChunkHeader;
import com.newrelic.jfr.model.ChunkMetadata;
import com.newrelic.jfr.model.ConstantPool;

import java.nio.ByteBuffer;

public class ChunkParser {

    private final ByteSupport bs = new ByteSupport();
    private final ChunkHeaderParser headerParser = new ChunkHeaderParser();
    private final ChunkMetadataParser metadataParser = new ChunkMetadataParser();
    private final ConstantPoolParser constantPoolParser = new ConstantPoolParser();

    public Chunk parse(byte[] chunk){
        var buff = ByteBuffer.wrap(chunk);
        var header = headerParser.parse(buff);
        var meta = metadataParser.parse(buff, header);
        var constantPool = constantPoolParser.parse(buff, header, meta);
        return null;
    }


}
