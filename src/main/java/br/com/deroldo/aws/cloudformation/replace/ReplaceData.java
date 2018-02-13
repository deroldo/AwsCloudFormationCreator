package br.com.deroldo.aws.cloudformation.replace;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ReplaceData {

    private JsonElement jsonElement;

    ReplaceData(JsonElement jsonElement){
        this.jsonElement = jsonElement;
    }

    public void replace(JsonElement father, JsonElement grandfather) {
        replaceFromFatherObject(father, grandfather);
    }

    private void replaceFromFatherObject(JsonElement father, JsonElement grandfather) {
        if (grandfather.isJsonArray()){
            replaceFromFatherObjectAndGrandfatherArray(father, grandfather);
        } else {
            replaceFromFatherObjectAndGrandfatherObject(father, grandfather);
        }
    }

    private void replaceFromFatherObjectAndGrandfatherObject(JsonElement father, JsonElement grandfather) {
        if (isFatherInsideArray(father, grandfather)) {
            grandfather.getAsJsonObject().entrySet().stream()
                    .filter(set -> set.getValue().isJsonArray())
                    .map(set -> set.getValue().getAsJsonArray())
                    .filter(array -> StreamSupport.stream(array.spliterator(), false)
                            .anyMatch(e -> e.equals(father)))
                    .forEach(array -> {
                        List<JsonElement> elements = StreamSupport.stream(array.spliterator(), false).collect(Collectors.toList());
                        for (int i = elements.size(); i > 0; i--) {
                            if (elements.get(i - 1).equals(father)){
                                array.set(i - 1, this.jsonElement);
                            }
                        }
                    });
        } else {
            grandfather.getAsJsonObject().entrySet().stream()
                    .filter(set -> set.getValue().equals(father))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .ifPresent(fatherName -> {
                        grandfather.getAsJsonObject().remove(fatherName);
                        grandfather.getAsJsonObject().add(fatherName, this.jsonElement);
                    });
        }
    }

    private void replaceFromFatherObjectAndGrandfatherArray(JsonElement father, JsonElement grandfather) {
        JsonArray grandfatherArray = grandfather.getAsJsonArray();
        List<JsonElement> elements = StreamSupport.stream(grandfatherArray.spliterator(), false).collect(Collectors.toList());
        for (int i = elements.size(); i > 0; i--) {
            if (elements.get(i - 1).equals(father)){
                grandfatherArray.set(i - 1, this.jsonElement);
            }
        }
    }

    private static boolean isFatherInsideArray(JsonElement father, JsonElement grandfather) {
        return grandfather.getAsJsonObject().entrySet().stream()
                .filter(set -> set.getValue().isJsonArray())
                .map(set -> set.getValue().getAsJsonArray())
                .anyMatch(array -> StreamSupport.stream(array.spliterator(), false)
                        .anyMatch(e -> e.equals(father)));
    }
}
