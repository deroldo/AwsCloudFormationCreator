package br.com.deroldo.aws.cloudformation;

import java.io.File;
import java.io.IOException;

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

        for (final String userResourceName : json.keySet()) {
            final JsonObject userResource = json.get(userResourceName).getAsJsonObject();
            final String templateName = userResource.get("Template").getAsString();
            final JsonObject template = getJsonObject(templateName);
            for (final String userResourceParamName : userResource.keySet()) {
                replaceParam(userResourceParamName, userResource.get(userResourceParamName), template);
            }
        }
    }

    private static void replaceParam (final String userResourceParamName, final JsonElement userResourceParamValue,
            final JsonObject template) {

        template.keySet().forEach(templateAttr -> {
            final JsonElement templateAttrValue = template.get(templateAttr);
            if (isListObject(templateAttrValue)){
                templateAttrValue.getAsJsonArray().forEach(element -> {
                    redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, element);
                });
            } else if (isObject(templateAttrValue)){
                replaceParam(userResourceParamName, userResourceParamValue, templateAttrValue.getAsJsonObject());
            } else {
                replace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue);
            }
        });

    }

    private static void replace (final String userResourceParamName, final JsonElement userResourceParamValue,
            final String templateAttr, final JsonElement templateAttrValue) {
        if (isPrimitiveValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)){
            System.out.println("replace " + templateAttrValue + " to " + userResourceParamValue.getAsString());
        }
    }

    private static void redirectArrayElement (final String userResourceParamName,
            final JsonElement userResourceParamValue,
            final String templateAttr, final JsonElement element) {
        if (element.isJsonArray()){
            element.getAsJsonArray().forEach(subElement -> {
                redirectArrayElement(userResourceParamName, userResourceParamValue, templateAttr, subElement);
            });
        } else if (element.isJsonObject()){
            replaceParam(userResourceParamName, userResourceParamValue, element.getAsJsonObject());
        } else {
            replace(userResourceParamName, userResourceParamValue, templateAttr, element);
        }
    }

    private static boolean isObject (final JsonElement templateAttrValue) {
        return templateAttrValue.isJsonObject();
    }

    private static boolean isListObject (final JsonElement templateAttrValue) {
        return templateAttrValue.isJsonArray();
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
