package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkHeader;
import com.newrelic.jfr.model.ChunkMetadata;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkMetadataParser {

    private final ByteSupport bs = new ByteSupport();
    private final MetadataTreeParser treeParser = new MetadataTreeParser();

    public ChunkMetadata parse(ByteBuffer buff, ChunkHeader header) {
        buff.position((int) header.getMetadataOffset());
        var size = (int) bs.readVarLen(buff);
        var typeId = buff.get();
        if (typeId != 0) {
            throw new JfrParseException("Not sure what to do with metadata type " + typeId);
        }
        var startTime = bs.readVarLen(buff);
        var duration = bs.readVarLen(buff);
        var metadataId = bs.readVarLen(buff);

//        parsePools(header, buff);

        var numPoolStrings = (int) bs.readVarLen(buff);
        var poolStrings = IntStream.range(0, numPoolStrings)
                .mapToObj(x -> bs.readString(buff))
                .collect(Collectors.toList());

        var rootNode = treeParser.readTreeNode(buff, poolStrings);


        return null;
    }


    private void parsePools(ChunkHeader header, ByteBuffer buff) {
        var offset = header.getConstantPoolOffset();
        buff.position((int) offset);
        var size = (int) bs.readVarLen(buff);
        var typeId = (int) bs.readVarLen(buff);
        var timestamp = bs.readVarLen(buff);
        var duration = bs.readVarLen(buff);

        var offsetToNextPool = bs.readVarLen(buff);
        var flush = bs.readVarLen(buff) != 0;   //?????

        parsePools(buff);
    }

    private void parsePools(ByteBuffer buff) {
        var ct = bs.readVarLen(buff);
        System.out.println("**DEBUG: parsing " + ct + " pools");
        for (int i = 0; i < ct; i++) {
            var constantPool = new ConstantPoolParser().parse(buff);
        }
    }

}
