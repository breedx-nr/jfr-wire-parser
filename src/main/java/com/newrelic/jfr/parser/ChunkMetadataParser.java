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

//        parsePools(header, buff); ??????

        var numConstantStrings = (int) bs.readVarLen(buff);
        var constantStrings = IntStream.range(0, numConstantStrings)
                .mapToObj(x -> bs.readString(buff))
                .collect(Collectors.toList());

        var rootNode = treeParser.readTreeNode(buff, constantStrings);

        return new ChunkMetadata(header, startTime, duration, metadataId, constantStrings, rootNode);
    }
}
