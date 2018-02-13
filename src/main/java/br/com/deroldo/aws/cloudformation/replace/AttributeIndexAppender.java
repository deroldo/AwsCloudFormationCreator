package br.com.deroldo.aws.cloudformation.replace;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AttributeIndexAppender {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    public static void appendIndexOnMainAttributesName(List<String> attributeNames, JsonObject awsJsonObject, JsonObject template) {
        int currentIndex = ATOMIC_INTEGER.incrementAndGet();
        attributeNames.forEach(attr -> Optional.ofNullable(template.get(attr)).ifPresent(outputs -> outputs.getAsJsonObject().entrySet().forEach(set -> {
            addIndex(set.getKey(), template.getAsJsonObject(), currentIndex);
            awsJsonObject.get(attr).getAsJsonObject().add(set.getKey() + currentIndex, set.getValue());
        })));
    }

    private static void addIndex(String key, JsonObject jsonObject, int index) {
        jsonObject.entrySet().forEach(set -> {
            if (set.getKey().equals("Ref") && set.getValue().getAsString().equals(key)){
                set.setValue(new JsonPrimitive(key + index));
            } else if (set.getKey().equals("Fn::FindInMap") && set.getValue().getAsJsonArray().get(0).getAsString().equals(key)) {
                set.getValue().getAsJsonArray().set(0, new JsonPrimitive(key + index));
            } else if (set.getKey().equals("Fn::If") && set.getValue().getAsJsonArray().get(0).getAsString().equals(key)) {
                set.getValue().getAsJsonArray().set(0, new JsonPrimitive(key + index));
            } else if (set.getKey().equals("Fn::GetAtt") && set.getValue().getAsJsonArray().get(0).getAsString().equals(key)) {
                set.getValue().getAsJsonArray().set(0, new JsonPrimitive(key + index));
            } else if (set.getKey().equals("DependsOn") && set.getValue().getAsString().equals(key)) {
                set.setValue(new JsonPrimitive(key + index));
            } else if (set.getKey().equals("Condition") && set.getValue().getAsString().equals(key)) {
                set.setValue(new JsonPrimitive(key + index));
            } else {
                if (set.getValue().isJsonObject()){
                    addIndex(key, set.getValue().getAsJsonObject(), index);
                } else if (set.getValue().isJsonArray()){
                    addIndexArray(key, set.getValue().getAsJsonArray(), index);
                } else if (set.getValue().getAsString().contains("${".concat(key).concat("}"))){
                    set.setValue(new JsonPrimitive(set.getValue().getAsString().replace("${".concat(key).concat("}"), "${".concat(key + index).concat("}"))));
                }
            }
        });
    }

    private static void addIndexArray(String key, JsonArray jsonArray, int index) {
        jsonArray.forEach(element -> {
            if (element.isJsonObject()){
                addIndex(key, element.getAsJsonObject(), index);
            } else if (element.isJsonArray()){
                addIndexArray(key, element.getAsJsonArray(), index);
            }
        });
    }

}
