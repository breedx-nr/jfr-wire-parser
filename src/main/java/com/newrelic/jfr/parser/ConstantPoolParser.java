package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ConstantPool;

import java.nio.ByteBuffer;

public class ConstantPoolParser {

    private final ByteSupport bs = new ByteSupport();

    ConstantPool parse(ByteBuffer buff) {
        var typeId = (int) bs.readVarLen(buff);
        var numConstants = (int)bs.readVarLen(buff);
        var key = bs.readVarLen(buff);

        return new ConstantPool();
    }

}
