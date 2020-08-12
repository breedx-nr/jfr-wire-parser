package com.newrelic.jfr.parser;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteSupport {

    public long readVarLen(ByteBuffer buff){
        var b = buff.get();
        return readVarLenRecursive(buff, b, 0, 0);
    }

    private long readVarLenRecursive(ByteBuffer buff, byte b, int bytepos, long acc){
        if(bytepos == 8){
            return acc + (((long) (b & 0XFF)) << 56);
        }
        long result = acc + ((b & 0x7FL) << (7*bytepos));
        if(b >= 0){
            return result;
        }
        return readVarLenRecursive(buff, buff.get(), bytepos+1, result);
    }
}
