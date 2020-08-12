package com.newrelic.jfr.model;

import java.util.List;

public class MetadataTreeNode {

    private final String name;
    private final List<StringPair> attributes;
    private final List<MetadataTreeNode> children;

    public MetadataTreeNode(String name, List<StringPair> attributes, List<MetadataTreeNode> children) {
        this.name = name;
        this.attributes = attributes;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public List<StringPair> getAttributes() {
        return attributes;
    }

    public List<MetadataTreeNode> getChildren() {
        return children;
    }
}
