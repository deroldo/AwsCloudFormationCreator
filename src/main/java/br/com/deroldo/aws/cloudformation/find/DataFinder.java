package br.com.deroldo.aws.cloudformation.find;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.replace.JsonType;
import br.com.deroldo.aws.cloudformation.replace.ReplaceDataFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataFinder {

    public static void findAndReplace (String userResourceParamName, JsonElement userResourceParamValue,
            JsonObject template, JsonType jsonType,
            CloudFormationPublisher publisher) {
        findAndReplace(userResourceParamName, userResourceParamValue, template, new ArrayList<>(), jsonType, publisher);
    }

    private static void findAndReplace (String userResourceParamName, JsonElement userResourceParamValue,
            JsonObject template, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher) {
        parents.add(template);

        new HashSet<>(template.keySet()).forEach(templateAttr -> {
            JsonElement templateAttrValue = template.get(templateAttr);

            if (templateAttrValue.isJsonArray()) {
                List<JsonElement> elements = StreamSupport.stream(templateAttrValue.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
                for (int i = elements.size(); i > 0; i--) {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, elements.get(i - 1), parents,
                            jsonType, publisher);
                }
            } else if (templateAttrValue.isJsonObject()) {
                findAndReplace(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject(), parents,
                        jsonType, publisher);
            } else {
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, parents, jsonType,
                        publisher);
            }
        });

        parents.remove(template);
    }

    private static void redirectArrayElement (String userResourceParamName, JsonElement userResourceParamValue,
            String templateAttr, JsonElement element, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher) {
        if (element.isJsonArray()) {
            parents.add(element);
            element.getAsJsonArray().forEach(subElement -> redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement, parents, jsonType,
                    publisher));
            parents.remove(element);
        } else if (element.isJsonObject()) {
            findAndReplace(userResourceParamName, userResourceParamValue, element.getAsJsonObject(), parents, jsonType,
                    publisher);
        } else {
            replace(userResourceParamName, userResourceParamValue, templateAttr, element, parents, jsonType, publisher);
        }
    }

    private static void replace (String userResourceParamName, JsonElement userResourceParamValue,
            String templateAttr, JsonElement templateAttrValue, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher) {
        ReplaceDataFactory.create(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, jsonType, publisher)
                .ifPresent(replaceData -> {
                    JsonElement father = parents.get(parents.size() - 1);
                    JsonElement grandfather = parents.get(parents.size() - 2);
                    replaceData.replace(father, grandfather);
                });
    }

}
