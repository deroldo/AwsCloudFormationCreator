package br.com.deroldo.aws.cloudformation.find;

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
                               JsonObject template, boolean isNumber) {
        findAndReplace(userResourceParamName, userResourceParamValue, template, new ArrayList<>(), isNumber);
    }

    private static void findAndReplace(String userResourceParamName, JsonElement userResourceParamValue,
                                JsonObject template, List<JsonElement> parents, boolean isNumber) {
        parents.add(template);

        new HashSet<>(template.keySet()).forEach(templateAttr -> {
            final JsonElement templateAttrValue = template.get(templateAttr);

            if (templateAttrValue.isJsonArray()) {
                // if attribute is an array, should run each node
                List<JsonElement> elements = StreamSupport.stream(templateAttrValue.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
                for (int i = elements.size(); i > 0; i--) {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, elements.get(i - 1), parents,
                            isNumber);
                }

            } else if (templateAttrValue.isJsonObject()) {
                // if attribute is an object, should run each sub attribute
                findAndReplace(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject(), parents,
                        isNumber);

            } else {
                // so, verify if need replace
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, parents, isNumber);
            }
        });

        parents.remove(template);
    }

    private static void redirectArrayElement(String userResourceParamName, JsonElement userResourceParamValue,
                                      String templateAttr, JsonElement element, List<JsonElement> parents, boolean isNumber) {
        if (element.isJsonArray()) {
            parents.add(element);
            element.getAsJsonArray().forEach(subElement -> redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement, parents, isNumber));
            parents.remove(element);
        } else if (element.isJsonObject()) {
            findAndReplace(userResourceParamName, userResourceParamValue, element.getAsJsonObject(), parents, isNumber);
        } else {
            replace(userResourceParamName, userResourceParamValue, templateAttr, element, parents, isNumber);
        }
    }

    private static void replace(String userResourceParamName, JsonElement userResourceParamValue,
                         String templateAttr, JsonElement templateAttrValue, List<JsonElement> parents, boolean isNumber) {
        ReplaceDataFactory.create(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, isNumber)
                .ifPresent(replaceData -> {
                    JsonElement father = parents.get(parents.size() - 1);
                    JsonElement grandfather = parents.get(parents.size() - 2);
                    replaceData.replace(father, grandfather);
                });
    }

}
