package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkMetadata;
import com.newrelic.jfr.model.MetadataTreeNode;
import com.newrelic.jfr.model.TypeDescriptor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeDescriptorIndexer {

    public Map<Integer, TypeDescriptor> index(ChunkMetadata meta){
        return meta.getRootNode().getChildren().stream()
                .filter(x -> x.getName().equals("metadata"))
                .findFirst()
                .get()
                .getChildren()
                .stream()
                .collect(Collectors.toMap(this::nodeId, this::buildTypeDescriptor));
    }

    private TypeDescriptor buildTypeDescriptor(MetadataTreeNode node){
        var id = nodeId(node);
        var name = node.attr("name");
        var fields = node.childrenNamed("field").stream()
                .map(this::buildField)
                .collect(Collectors.toList());
        var annotations = node.childrenNamed("annotation").stream()
                .map(this::buildAnnotation)
                .collect(Collectors.toList());
        return new TypeDescriptor(id, name, fields, annotations);
    }

    private TypeDescriptor.Field buildField(MetadataTreeNode node) {
        var name = node.attr("name");
        var classId = Integer.parseInt(node.attr("class"));
        var cp = node.maybeAttr("constantPool").orElse("false");
        return new TypeDescriptor.Field(name, classId, "true".equals(cp));
    }

    private TypeDescriptor.Annotation buildAnnotation(MetadataTreeNode metadataTreeNode) {
        return null;
    }

    private Integer nodeId(MetadataTreeNode n){
        return Integer.parseInt(n.attr("id"));
    }

}
