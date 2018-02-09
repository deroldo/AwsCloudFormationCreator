package br.com.deroldo.aws.cloudformation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Test {

    private static final ObjectMapper YML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Gson GSON = new Gson();

    public static void main (final String[] args) throws IOException {
        final JsonObject json = getJsonObject("teste");

        // aws object
        JsonObject awsJsonObject = new JsonObject();
        awsJsonObject.add("Resources", new JsonObject());
        awsJsonObject.add("Outputs", new JsonObject());
        awsJsonObject.add("Mappings", new JsonObject());

        // aux variable to index resources, outputs and mappings names to always be different
        final AtomicInteger index = new AtomicInteger(1);

        for (final String userResourceName : json.keySet()) {
            final JsonObject userResource = json.get(userResourceName).getAsJsonObject();
            final String templateName = userResource.get("Template").getAsString();
            final JsonObject template = getJsonObject(templateName);

            // replace with user params
            userResource.keySet().forEach(userResourceParamName -> replaceParam(userResourceParamName, userResource.get(userResourceParamName), template, new ArrayList<>()));

            // replace with defaults params
            JsonObject parameters = template.get("Parameters").getAsJsonObject();
            parameters.getAsJsonObject().keySet().stream()
                    .filter(templateResourceParamName -> Objects.nonNull(parameters.get(templateResourceParamName).getAsJsonObject().get("Default")))
                    .forEach(templateResourceParamName -> replaceParam(templateResourceParamName, parameters.get(templateResourceParamName).getAsJsonObject().get("Default"), template, new ArrayList<>()));

            // adding resources with params replaced on aws object
            template.get("Resources").getAsJsonObject().entrySet().forEach(set -> awsJsonObject.get("Resources").getAsJsonObject().add(set.getKey() + index.get(), set.getValue()));

            // adding outputs with params replaced on aws object
            Optional.ofNullable(template.get("Outputs")).ifPresent(outputs -> outputs.getAsJsonObject().entrySet().forEach(set -> {
                awsJsonObject.get("Outputs").getAsJsonObject().add(set.getKey() + index.get(), set.getValue());
            }));

            // adding mappings with params replaced on aws object
            Optional.ofNullable(template.get("Mappings")).ifPresent(outputs -> outputs.getAsJsonObject().entrySet().forEach(set -> {
                awsJsonObject.get("Mappings").getAsJsonObject().add(set.getKey() + index.get(), set.getValue());
            }));

            index.incrementAndGet();
        }

        // getting aws yml
        String awsYml = getAwsYml(awsJsonObject);

        System.out.println(awsYml);
    }

    private static String getAwsYml(JsonObject awsJsonObject) throws IOException {
        String awsJson = GSON.toJson(awsJsonObject);
        JsonNode awsJsonNode = JSON_MAPPER.readValue(awsJson, JsonNode.class);
        return YML_MAPPER.writeValueAsString(awsJsonNode);
    }

    private static void replaceParam (final String userResourceParamName, final JsonElement userResourceParamValue,
            final JsonObject template, List<JsonElement> parents) {
        parents.add(template);

        new HashSet<>(template.keySet()).forEach(templateAttr -> {
            final JsonElement templateAttrValue = template.get(templateAttr);

            if (templateAttrValue.isJsonArray()){
                // if attribute is an array, should run each node
                List<JsonElement> elements = StreamSupport.stream(templateAttrValue.getAsJsonArray().spliterator(), false).collect(Collectors.toList());
                for (int i = elements.size(); i > 0; i--) {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, elements.get(i - 1), parents);
                }

            } else if (templateAttrValue.isJsonObject()){
                // if attribute is an object, should run each sub attribute
                replaceParam(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject(), parents);

            } else {
                // so, verify if need replace
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue, parents);
            }
        });

        parents.remove(template);
    }

    private static void replace (final String userResourceParamName, final JsonElement userResourceParamValue,
            final String templateAttr, final JsonElement templateAttrValue, List<JsonElement> parents) {

        if (isPrimitiveValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)){
            JsonElement father = parents.get(parents.size() - 1);
            JsonElement grandfather = parents.get(parents.size() - 2);

            if (father.isJsonArray()){
                if (grandfather.isJsonArray()){
                    // is a father array and grandfather array
                    System.out.println("father array and grandfather array");
                } else {
                    // is a father array and grandfather object
                    System.out.println("father array and grandfather object");
                }
            } else {
                if (grandfather.isJsonArray()){
                    // is a father object and grandfather array
                    System.out.println("father object and grandfather array");
                } else {
                    // is a father object and grandfather object

                    if (isFatherInsideArray(father, grandfather)) {
                        // replace value inside a grandfather value array
                        grandfather.getAsJsonObject().entrySet().stream()
                                .filter(set -> set.getValue().isJsonArray())
                                .map(set -> set.getValue().getAsJsonArray())
                                .filter(array -> StreamSupport.stream(array.spliterator(), false)
                                        .anyMatch(e -> e.equals(father)))
                                .forEach(array -> {
                                    array.remove(father);
                                    array.add(userResourceParamValue.getAsString());
                                });
                    } else {
                        // replace value
                        String fatherName = grandfather.getAsJsonObject().entrySet().stream()
                                .filter(set -> set.getValue().equals(father))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElseThrow(RuntimeException::new);
                        grandfather.getAsJsonObject().remove(fatherName);
                        grandfather.getAsJsonObject().addProperty(fatherName, userResourceParamValue.getAsString());
                    }
                }
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

    private static void redirectArrayElement (final String userResourceParamName,
            final JsonElement userResourceParamValue,
            final String templateAttr, final JsonElement element, List<JsonElement> parents) {
        if (element.isJsonArray()){
            // if element is another array, should run each node
            parents.add(element);
            element.getAsJsonArray().forEach(subElement -> redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement, parents));
        } else if (element.isJsonObject()){
            // if element inside array is an object, should run each attribute
            replaceParam(userResourceParamName, userResourceParamValue, element.getAsJsonObject(), parents);
        } else {
            // so, verify if need replace
            replace(userResourceParamName, userResourceParamValue, templateAttr, element, parents);
        }
    }

    private static boolean isPrimitiveValidToReplace (final String userResourceParamName,
            final JsonElement userResourceParamValue, final String templateAttr, final JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonPrimitive() && "Ref".equals(templateAttr)
                && templateAttrValue.isJsonPrimitive() && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static String getPath (final String fileName) {
        return Main.class.getClassLoader().getResource(fileName + ".yml").getPath();
    }

    private static JsonObject getJsonObject (final String fileName) throws IOException {
        return getJsonObject(fileName, JsonObject.class);
    }

    private static <T> T getJsonObject (final String fileName, final Class<T> type) throws IOException {
        final JsonNode userData = YML_MAPPER.readValue(new File(getPath(fileName)), JsonNode.class);
        final String userDataJson = JSON_MAPPER.writeValueAsString(userData);
        return GSON.fromJson(userDataJson, type);
    }

}
