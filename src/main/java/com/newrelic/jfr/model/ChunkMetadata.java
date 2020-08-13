package com.newrelic.jfr.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkMetadata {
    private final ChunkHeader header;
    private final long startTime;
    private final long duration;
    private final long metadataId;
    private final List<String> poolStrings;
    private final MetadataTreeNode rootNode;

    public ChunkMetadata(ChunkHeader header, long startTime, long duration, long metadataId, List<String> poolStrings, MetadataTreeNode rootNode) {
        this.header = header;
        this.startTime = startTime;
        this.duration = duration;
        this.metadataId = metadataId;
        this.poolStrings = poolStrings;
        this.rootNode = rootNode;
    }

    public ChunkHeader getHeader() {
        return header;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public long getMetadataId() {
        return metadataId;
    }

    public List<String> getPoolStrings() {
        return poolStrings;
    }

    public MetadataTreeNode getRootNode() {
        return rootNode;
    }

    public Map<Integer,MetadataTreeNode> getMetadataChildrenIndexedById(){
        return rootNode.getChildren().stream()
                .filter(x -> x.getName().equals("metadata"))
                .findFirst()
                .get()
                .getChildren()
                .stream()
                .collect(Collectors.toMap(a -> Integer.parseInt(a.attr("id")), a -> a));
    }
}
