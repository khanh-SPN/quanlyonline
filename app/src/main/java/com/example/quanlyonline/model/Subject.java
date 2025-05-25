package com.example.quanlyonline.model;

public class Subject {
    private String subjectId;
    private ValueWrapper name;
    private ValueWrapper classId;

    public Subject() {}

    // Class lồng nhau để ánh xạ các trường có dạng { "value": "..." }
    public static class ValueWrapper {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getName() { return name != null ? name.getValue() : null; }
    public void setName(String name) {
        this.name = new ValueWrapper();
        this.name.setValue(name);
    }

    public String getClassId() { return classId != null ? classId.getValue() : null; }
    public void setClassId(String classId) {
        this.classId = new ValueWrapper();
        this.classId.setValue(classId);
    }
}