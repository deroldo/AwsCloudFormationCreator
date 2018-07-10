package br.com.deroldo.aws.cloudformation.userdata;

import br.com.deroldo.aws.cloudformation.find.DataFinder;
import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;
import br.com.deroldo.aws.cloudformation.publish.TemplateCapability;
import br.com.deroldo.aws.cloudformation.replace.AttributeIndexAppender;
import br.com.deroldo.aws.cloudformation.replace.JsonType;
import com.amazonaws.services.cloudformation.model.Capability;
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

public class InterpreterUserData {

    private static List<String> MAIN_ATTRS = Arrays.asList("Resources", "Outputs", "Mappings", "Conditions");

    private static ObjectMapper YML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static Gson GSON = new Gson();

    private static String GLOBAL_PARAMETERS = "GlobalParameters";
    private static String TEMPLATE = "Template";
    private static String PARAMETERS = "Parameters";
    private static String TYPE = "Type";
    private static String DEFAULT = "Default";
    private static String REF = "Ref";

    private InputStream file;

    public InterpreterUserData(InputStream file) {
        this.file = file;
    }

    public YmlData interpretAndGetYmlData (CloudFormationPublisher publisher) throws IOException {
        JsonObject userDataObject = getJsonObject(this.file);
        JsonObject awsJsonObject = initializeAwsObject();

        Set<String> globalParams = getGlobalParameters(userDataObject);
        Set<String> userDataResources = getAllUserResources(userDataObject);

        List<Capability> capabilities = new ArrayList<>();
        for (String userResourceName : userDataResources) {
            TemplateCapability templateCapability = findAndReplace(userDataObject, awsJsonObject, globalParams, userResourceName, publisher);
            if (!TemplateCapability.DEFAULT.equals(templateCapability)){
                capabilities.add(templateCapability.getCapability());
            }
        }

        validateIfThereIsRefNotReplaced(awsJsonObject);
        removeEmptyAttribute(awsJsonObject);

        return new YmlData(getAwsYml(awsJsonObject), capabilities);
    }

    private TemplateCapability findAndReplace (JsonObject userDataObject, JsonObject awsJsonObject,
            Set<String> globalParams, String userResourceName,
            CloudFormationPublisher publisher) throws IOException {
        JsonObject userResource = userDataObject.get(userResourceName).getAsJsonObject();
        String templateName = userResource.get(TEMPLATE).getAsString();
        JsonObject template = getJsonObject(templateName);
        JsonObject parameters = template.get(PARAMETERS).getAsJsonObject();

        findAndReplaceToUserParam(userResource, template, parameters, publisher);
        findAndReplaceToGlobalParam(userDataObject, globalParams, template, parameters, publisher);
        findAndReplaceToDefaultParam(template, parameters, publisher);

        AttributeIndexAppender.appendIndexOnMainAttributesName(MAIN_ATTRS, awsJsonObject, template);

        return TemplateCapability.get(templateName);
    }

    private void findAndReplaceToDefaultParam (JsonObject template, JsonObject parameters,
            CloudFormationPublisher publisher) {
        parameters.getAsJsonObject().keySet().stream()
                .filter(templateResourceParamName -> Objects.nonNull(parameters.get(templateResourceParamName).getAsJsonObject().get(DEFAULT)))
                .forEach(templateResourceParamName -> {
                    JsonObject parameter = parameters.get(templateResourceParamName).getAsJsonObject();
                    JsonType jsonType = JsonType.get(parameter.get(TYPE).getAsString());
                    DataFinder.findAndReplace(templateResourceParamName, parameter.get(DEFAULT), template, jsonType, publisher);
                });
    }

    private void findAndReplaceToGlobalParam (JsonObject userDataObject, Set<String> globalParams, JsonObject template,
            JsonObject parameters, CloudFormationPublisher publisher) {
        globalParams.forEach(userResourceParamName -> {
            JsonType jsonType = getJsonType(parameters, userResourceParamName);
            DataFinder.findAndReplace(userResourceParamName, userDataObject.get(GLOBAL_PARAMETERS).getAsJsonObject().get(userResourceParamName), template, jsonType, publisher);
        });
    }

    private void findAndReplaceToUserParam (JsonObject userResource, JsonObject template, JsonObject parameters,
            CloudFormationPublisher publisher) {
        userResource.keySet().forEach(userResourceParamName -> {
            JsonType jsonType = getJsonType(parameters, userResourceParamName);
            DataFinder.findAndReplace(userResourceParamName, userResource.get(userResourceParamName), template, jsonType, publisher);
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

        if (!nonDeclaredParams.isEmpty()) {
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

    private static JsonType getJsonType(JsonObject parameters, String userResourceParamName) {
        return Optional.ofNullable(parameters.get(userResourceParamName))
                .map(JsonElement::getAsJsonObject)
                .map(parameter -> parameter.get(TYPE))
                .map(JsonElement::getAsString)
                .map(JsonType::get)
                .orElse(JsonType.STRING);
    }

    private static Set<String> getAllRefs(JsonObject awsJsonObject) {
        Set<String> refs = new HashSet<>();
        awsJsonObject.entrySet().forEach(set -> {
            if (set.getKey().equals(REF)) {
                refs.add(set.getValue().getAsString());
            } else {
                if (set.getValue().isJsonObject()) {
                    refs.addAll(getAllRefs(set.getValue().getAsJsonObject()));
                } else if (set.getValue().isJsonArray()) {
                    refs.addAll(getAllRefsFromArray(set.getValue().getAsJsonArray()));
                }
            }
        });
        return refs;
    }

    private static Set<String> getAllRefsFromArray(JsonArray asJsonArray) {
        Set<String> refs = new HashSet<>();
        asJsonArray.forEach(element -> {
            if (element.isJsonObject()) {
                refs.addAll(getAllRefs(element.getAsJsonObject()));
            } else if (element.isJsonArray()) {
                refs.addAll(getAllRefsFromArray(element.getAsJsonArray()));
            }
        });
        return refs;
    }

    private static String getAwsYml(JsonObject awsJsonObject) throws IOException {
        String awsJson = GSON.toJson(awsJsonObject);
        JsonNode awsJsonNode = JSON_MAPPER.readValue(awsJson, JsonNode.class);
        return YML_MAPPER.writeValueAsString(awsJsonNode);
    }

    private static InputStream getInputStream(String fileName) {
        return InterpreterUserData.class.getClassLoader().getResourceAsStream(fileName);
    }

    private static JsonObject getJsonObject(String fileName) throws IOException {
        String name = fileName.replace(".yml", "").replace(".yaml", "").concat(".yml");
        return getJsonObject(getInputStream(name));
    }

    private static JsonObject getJsonObject(InputStream file) throws IOException {
        JsonNode userData = YML_MAPPER.readValue(file, JsonNode.class);
        final String userDataJson = JSON_MAPPER.writeValueAsString(userData);
        final String replacedUserDataJson = Optional.ofNullable(System.getProperty("GIT_HASH")).map(gitHash -> userDataJson.replace("${gitHash}", gitHash)).orElse(userDataJson);
        return GSON.fromJson(replacedUserDataJson, JsonObject.class);
    }

}
