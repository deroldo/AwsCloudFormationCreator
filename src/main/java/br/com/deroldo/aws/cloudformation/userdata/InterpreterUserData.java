package br.com.deroldo.aws.cloudformation.userdata;

import br.com.deroldo.aws.cloudformation.find.DataFinder;
import br.com.deroldo.aws.cloudformation.replace.AttributeIndexAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class InterpreterUserData {

    private static final List<String> MAIN_ATTRS = Arrays.asList("Resources", "Outputs", "Mappings", "Conditions");

    private static final ObjectMapper YML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Gson GSON = new Gson();

    private static final String GLOBAL_PARAMETERS = "GlobalParameters";
    private static final String TEMPLATE = "Template";
    private static final String PARAMETERS = "Parameters";
    private static final String TYPE = "Type";
    private static final String NUMBER = "Number";
    private static final String DEFAULT = "Default";
    private static final String REF = "Ref";

    private String fileName;

    public InterpreterUserData(String fileName){
        this.fileName = fileName;
    }

    public String interpretAndGetYml() {
        JsonObject userDataObject = getJsonObject(this.fileName);
        JsonObject awsJsonObject = initializeAwsObject();

        Set<String> globalParams = getGlobalParameters(userDataObject);
        Set<String> userDataResources = getAllUserResources(userDataObject);

        userDataResources.forEach(userResourceName -> findAndReplace(userDataObject, awsJsonObject, globalParams, userResourceName));

        validateIfThereIsRefNotReplaced(awsJsonObject);
        removeEmptyAttribute(awsJsonObject);

        return getAwsYml(awsJsonObject);
    }

    private void findAndReplace(JsonObject userDataObject, JsonObject awsJsonObject, Set<String> globalParams, String userResourceName) {
        JsonObject userResource = userDataObject.get(userResourceName).getAsJsonObject();
        String templateName = userResource.get(TEMPLATE).getAsString();
        JsonObject template = getJsonObject(templateName);
        JsonObject parameters = template.get(PARAMETERS).getAsJsonObject();

        findAndReplaceToUserParam(userResource, template, parameters);
        findAndReplaceToGlobalParam(userDataObject, globalParams, template, parameters);
        findAndReplaceToDefaultParam(template, parameters);

        AttributeIndexAppender.appendIndexOnMainAttributesName(MAIN_ATTRS, awsJsonObject, template);
    }

    private void findAndReplaceToDefaultParam(JsonObject template, JsonObject parameters) {
        parameters.getAsJsonObject().keySet().stream()
                .filter(templateResourceParamName -> Objects.nonNull(parameters.get(templateResourceParamName).getAsJsonObject().get(DEFAULT)))
                .forEach(templateResourceParamName -> {
                    JsonObject parameter = parameters.get(templateResourceParamName).getAsJsonObject();
                    boolean isNumber = parameter.get(TYPE).getAsString().equals(NUMBER);
                    DataFinder.findAndReplace(templateResourceParamName, parameter.get(DEFAULT), template, isNumber);
                });
    }

    private void findAndReplaceToGlobalParam(JsonObject userDataObject, Set<String> globalParams, JsonObject template, JsonObject parameters) {
        globalParams.forEach(userResourceParamName -> {
            boolean isNumber = isNumber(parameters, userResourceParamName);
            DataFinder.findAndReplace(userResourceParamName, userDataObject.get(GLOBAL_PARAMETERS).getAsJsonObject().get(userResourceParamName), template, isNumber);
        });
    }

    private void findAndReplaceToUserParam(JsonObject userResource, JsonObject template, JsonObject parameters) {
        userResource.keySet().forEach(userResourceParamName -> {
            boolean isNumber = isNumber(parameters, userResourceParamName);
            DataFinder.findAndReplace(userResourceParamName, userResource.get(userResourceParamName), template, isNumber);
        });
    }

    private void removeEmptyAttribute(JsonObject awsJsonObject) {
        MAIN_ATTRS.stream()
                .filter(attr -> awsJsonObject.get(attr).getAsJsonObject().keySet().isEmpty())
                .forEach(awsJsonObject::remove);
    }

    private void validateIfThereIsRefNotReplaced(JsonObject awsJsonObject) {
        Set<String> nonDeclaredParams = getAllRefs(awsJsonObject).stream()
                .filter(ref -> !ref.startsWith("AWS::"))
                .filter(ref -> !awsJsonObject.get("Resources").getAsJsonObject().keySet().contains(ref))
                .collect(Collectors.toSet());

        if (!nonDeclaredParams.isEmpty()){
            throw new RuntimeException(format("These params %s must be declared, they don't have a default value", nonDeclaredParams.toString()));
        }
    }

    private Set<String> getAllUserResources(JsonObject json) {
        return json.keySet().stream()
                    .filter(key -> !key.equals(GLOBAL_PARAMETERS)).collect(Collectors.toSet());
    }

    private Set<String> getGlobalParameters(JsonObject json) {
        return Optional.ofNullable(json.get(GLOBAL_PARAMETERS))
                    .map(JsonElement::getAsJsonObject)
                    .map(JsonObject::keySet)
                    .orElse(Collections.emptySet());
    }

    private JsonObject initializeAwsObject() {
        JsonObject awsJsonObject = new JsonObject();
        MAIN_ATTRS.forEach(attr -> awsJsonObject.add(attr, new JsonObject()));
        return awsJsonObject;
    }

    private static boolean isNumber(JsonObject parameters, String userResourceParamName) {
        return Optional.ofNullable(parameters.get(userResourceParamName))
                .map(JsonElement::getAsJsonObject)
                .map(parameter -> parameter.get(TYPE))
                .map(JsonElement::getAsString)
                .map(parameterType -> parameterType.equals(NUMBER))
                .orElse(false);
    }

    private static Set<String> getAllRefs(JsonObject awsJsonObject) {
        Set<String> refs = new HashSet<>();
        awsJsonObject.entrySet().forEach(set -> {
            if (set.getKey().equals(REF)){
                refs.add(set.getValue().getAsString());
            } else {
                if (set.getValue().isJsonObject()){
                    refs.addAll(getAllRefs(set.getValue().getAsJsonObject()));
                } else if (set.getValue().isJsonArray()){
                    refs.addAll(getAllRefsFromArray(set.getValue().getAsJsonArray()));
                }
            }
        });
        return refs;
    }

    private static Set<String> getAllRefsFromArray(JsonArray asJsonArray) {
        Set<String> refs = new HashSet<>();
        asJsonArray.forEach(element -> {
            if (element.isJsonObject()){
                refs.addAll(getAllRefs(element.getAsJsonObject()));
            } else if (element.isJsonArray()){
                refs.addAll(getAllRefsFromArray(element.getAsJsonArray()));
            }
        });
        return refs;
    }

    private static String getAwsYml(JsonObject awsJsonObject) {
        try {
            String awsJson = GSON.toJson(awsJsonObject);
            JsonNode awsJsonNode = JSON_MAPPER.readValue(awsJson, JsonNode.class);
            return YML_MAPPER.writeValueAsString(awsJsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream getInputStream(final String fileName) {
        String name = fileName.replace(".yml", "").replace(".yaml", "");
        return InterpreterUserData.class.getClassLoader().getResourceAsStream(name + ".yml");
    }

    private static JsonObject getJsonObject (final String fileName) {
        return getJsonObject(fileName, JsonObject.class);
    }

    private static <T> T getJsonObject (final String fileName, final Class<T> type)  {
        try {
            JsonNode userData = YML_MAPPER.readValue(getInputStream(fileName), JsonNode.class);
            final String userDataJson = JSON_MAPPER.writeValueAsString(userData);
            return GSON.fromJson(userDataJson, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}