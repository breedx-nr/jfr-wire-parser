package com.newrelic.jfr.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public String attr(String key){
        return maybeAttr(key).get();
    }

    public Optional<String> maybeAttr(String key){
        return attributes.stream()
                .filter(a -> a.getKey().equals(key))
                .map(KeyValuePair::getValue)
                .findFirst();
    }

    public List<MetadataTreeNode> childrenNamed(String name){
        return children.stream()
                .filter(child -> child.getName().equals(name))
                .collect(Collectors.toList());
    }
}
