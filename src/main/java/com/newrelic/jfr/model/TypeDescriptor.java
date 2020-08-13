package com.newrelic.jfr.model;

import java.util.List;

// Describes a type and what it is composed of
public class TypeDescriptor {

    private final long id;
    private final String name;
    private final List<Field> fields;
    private final List<Annotation> annotations;

    public TypeDescriptor(long id, String name, List<Field> fields, List<Annotation> annotations) {
        this.id = id;
        this.name = name;
        this.fields = fields;
        this.annotations = annotations;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isPrimitive(){
        return fields.isEmpty();
    }

    public static class Field {
        private final String name;
        private final int classId;
        private final boolean constantPool;

        public Field(String name, int classId, boolean constantPool) {
            this.name = name;
            this.classId = classId;
            this.constantPool = constantPool;
        }

        public String getName() {
            return name;
        }

        public int getClassId() {
            return classId;
        }

        public boolean isConstantPool() {
            return constantPool;
        }
    }

    public static class Annotation {
        private final String value;
        private final int classId;

        public Annotation(String value, int classId) {
            this.value = value;
            this.classId = classId;
        }

        public String getValue() {
            return value;
        }

        public int getClassId() {
            return classId;
        }
    }
}
