package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkHeader;

import java.nio.ByteBuffer;

public class ChunkHeaderParser {

    public ChunkHeader parse(byte[] chunk){
        ByteBuffer buff = ByteBuffer.wrap(chunk);

    }
}
