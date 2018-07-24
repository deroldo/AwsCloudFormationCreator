package br.com.deroldo.aws.cloudformation.find;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.replace.JsonType;
import br.com.deroldo.aws.cloudformation.replace.ReplaceData;
import br.com.deroldo.aws.cloudformation.replace.ReplaceDataFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DataFinder {

    public static void findAndReplace (String userResourceParamName, JsonElement userResourceParamValue,
            JsonObject template, JsonType jsonType, CloudFormationPublisher publisher, Set<String> userDataResources, JsonObject userResource, JsonObject userDataObject) {
        findAndReplace(userResourceParamName, userResourceParamValue, template, new ArrayList<>(), jsonType, publisher, userDataResources, userResource, userDataObject);
    }

    private static void findAndReplace (String userResourceParamName, JsonElement userResourceParamValue,
            JsonObject template, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher, Set<String> userDataResources, JsonObject userResource, JsonObject userDataObject) {
        parents.add(template);

        new HashSet<>(template.keySet()).forEach(templateAttr -> {
            JsonElement templateAttrValue = template.get(templateAttr);

            if (templateAttrValue.isJsonArray()) {
                List<JsonElement> elements = StreamSupport.stream(templateAttrValue.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
                for (int i = elements.size(); i > 0; i--) {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, elements.get(i - 1), parents,
                            jsonType, publisher, userDataResources, userResource, userDataObject);
                }
            } else if (templateAttrValue.isJsonObject()) {
                findAndReplace(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject(), parents,
                        jsonType, publisher, userDataResources, userResource, userDataObject);
            } else {
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, parents, jsonType,
                        publisher, userDataResources, userResource, userDataObject);
            }
        });

        parents.remove(template);
    }

    private static void redirectArrayElement (String userResourceParamName, JsonElement userResourceParamValue,
            String templateAttr, JsonElement element, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher, Set<String> userDataResources, JsonObject userResource, JsonObject userDataObject) {
        if (element.isJsonArray()) {
            parents.add(element);
            element.getAsJsonArray().forEach(subElement -> redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement, parents, jsonType,
                    publisher, userDataResources, userResource, userDataObject));
            parents.remove(element);
        } else if (element.isJsonObject()) {
            findAndReplace(userResourceParamName, userResourceParamValue, element.getAsJsonObject(), parents, jsonType,
                    publisher, userDataResources, userResource, userDataObject);
        } else {
            replace(userResourceParamName, userResourceParamValue, templateAttr, element, parents, jsonType, publisher, userDataResources, userResource, userDataObject);
        }
    }

    private static void replace (String userResourceParamName, JsonElement userResourceParamValue,
            String templateAttr, JsonElement templateAttrValue, List<JsonElement> parents, JsonType jsonType,
            CloudFormationPublisher publisher, Set<String> userDataResources, JsonObject userResource, JsonObject userDataObject) {
        ReplaceDataFactory.create(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, jsonType, publisher)
                .ifPresent(replaceData -> {
                    JsonElement father = parents.get(parents.size() - 1);
                    JsonElement grandfather = parents.get(parents.size() - 2);

                    boolean grandfatherIsRef = grandfather.isJsonObject() && grandfather.getAsJsonObject().has("Ref");
                    boolean fatherIsRef = father.isJsonObject() && father.getAsJsonObject().has("Ref");

                    replaceData.replace(father, grandfather, grandfatherIsRef && fatherIsRef, userDataResources, userDataObject);
                });
    }

}
