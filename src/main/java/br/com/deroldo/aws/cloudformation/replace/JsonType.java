package br.com.deroldo.aws.cloudformation.replace;

import java.util.Arrays;

public enum JsonType {

    STRING("String"),
    NUMBER("Number"),
    COMMA_DELIMITED_LIST("CommaDelimitedList");

    private String typeName;

    JsonType(String typeName) {
        this.typeName = typeName;
    }

    public static JsonType get(String typeName){
        return Arrays.stream(JsonType.values())
                .filter(jt -> jt.getTypeName().equals(typeName))
                .findFirst()
                .orElse(JsonType.STRING);
    }

    public String getTypeName() {
        return typeName;
    }
}
