package br.com.deroldo.aws.cloudformation.find;

import br.com.deroldo.aws.cloudformation.replace.JsonType;
import br.com.deroldo.aws.cloudformation.replace.ReplaceDataFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DataFinder {

    public static void findAndReplace(String userResourceParamName, JsonElement userResourceParamValue,
                               JsonObject template, JsonType jsonType) {
        findAndReplace(userResourceParamName, userResourceParamValue, template, new ArrayList<>(), jsonType);
    }

    private static void findAndReplace(String userResourceParamName, JsonElement userResourceParamValue,
                                JsonObject template, List<JsonElement> parents, JsonType jsonType) {
        parents.add(template);

        new HashSet<>(template.keySet()).forEach(templateAttr -> {
            final JsonElement templateAttrValue = template.get(templateAttr);

            if (templateAttrValue.isJsonArray()) {
                List<JsonElement> elements = StreamSupport.stream(templateAttrValue.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
                for (int i = elements.size(); i > 0; i--) {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, elements.get(i - 1), parents,
                            jsonType);
                }
            } else if (templateAttrValue.isJsonObject()) {
                findAndReplace(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject(), parents,
                        jsonType);
            } else {
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, parents, jsonType);
            }
        });

        parents.remove(template);
    }

    private static void redirectArrayElement(String userResourceParamName, JsonElement userResourceParamValue,
                                      String templateAttr, JsonElement element, List<JsonElement> parents, JsonType jsonType) {
        if (element.isJsonArray()) {
            parents.add(element);
            element.getAsJsonArray().forEach(subElement -> redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement, parents, jsonType));
            parents.remove(element);
        } else if (element.isJsonObject()) {
            findAndReplace(userResourceParamName, userResourceParamValue, element.getAsJsonObject(), parents, jsonType);
        } else {
            replace(userResourceParamName, userResourceParamValue, templateAttr, element, parents, jsonType);
        }
    }

    private static void replace(String userResourceParamName, JsonElement userResourceParamValue,
                         String templateAttr, JsonElement templateAttrValue, List<JsonElement> parents, JsonType jsonType) {
        ReplaceDataFactory.create(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, jsonType)
                .ifPresent(replaceData -> {
                    JsonElement father = parents.get(parents.size() - 1);
                    JsonElement grandfather = parents.get(parents.size() - 2);
                    replaceData.replace(father, grandfather);
                });
    }

}
