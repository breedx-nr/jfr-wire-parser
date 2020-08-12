package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.MetadataTreeNode;
import com.newrelic.jfr.model.StringPair;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetadataTreeParser {

    private final ByteSupport bs = new ByteSupport();

    public MetadataTreeNode readTreeNode(ByteBuffer buff, List<String> poolStrings) {
        var name = bs.poolString(buff, poolStrings);
        var attributes = readAttributes(buff, poolStrings);
        var children = readChildNodes(buff, poolStrings);
        return new MetadataTreeNode(name, attributes, children);
    }

    private List<StringPair> readAttributes(ByteBuffer buff, List<String> poolStrings) {
        var num = (int) bs.readVarLen(buff);
        return IntStream.range(0, num)
                .mapToObj(x -> new StringPair(bs.poolString(buff, poolStrings), bs.poolString(buff, poolStrings)))
                .collect(Collectors.toList());
    }

    private List<MetadataTreeNode> readChildNodes(ByteBuffer buff, List<String> poolStrings) {
        var num = (int) bs.readVarLen(buff);
        return IntStream.range(0, num)
                .mapToObj(x -> readTreeNode(buff, poolStrings))
                .collect(Collectors.toList());
    }
}
