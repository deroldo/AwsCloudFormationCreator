package br.com.deroldo.aws.cloudformation.replace;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import br.com.deroldo.aws.cloudformation.publish.CloudFormationPublisher;

public class ReplaceDataFactory {

    private static final String RESOURCE_ID = "^(ResourceId::[a-zA-Z0-9-_\\.]+::[a-zA-Z0-9-_\\.]+)$";
    private static final String OUTPUT = "^(Output::[a-zA-Z0-9-_\\.]+::[a-zA-Z0-9-_\\.]+)$";

    public static Optional<ReplaceData> create(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue, JsonType jsonType, CloudFormationPublisher publisher) {
        Optional<JsonElement> element = Optional.empty();
        if (isPrimitiveValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)) {
            element = Optional.of(getJsonPrimitive(userResourceParamValue, jsonType, publisher));
        } else if (isMapValidToReplace(userResourceParamName, templateAttr, templateAttrValue)) {
            element = Optional.of(userResourceParamValue);
        } else if (isSubValidToReplace(userResourceParamName, templateAttr, templateAttrValue)) {
            element = Optional.of(getSubJsonPrimitive(userResourceParamValue, userResourceParamName, templateAttrValue));
        }
        return element.map(ReplaceData::new);
    }

    private static boolean isPrimitiveValidToReplace(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonPrimitive() && "Ref".equals(templateAttr) && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static boolean isMapValidToReplace(String userResourceParamName, String templateAttr, JsonElement templateAttrValue) {
        return templateAttr.equals("Fn::FindInMap") && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static boolean isSubValidToReplace(String userResourceParamName, String templateAttr, JsonElement templateAttrValue) {
        return templateAttr.equals("Fn::Sub") && templateAttrValue.getAsString().contains("${".concat(userResourceParamName).concat("}"));
    }

    private static JsonElement getJsonPrimitive (JsonElement userResourceParamValue, JsonType jsonType, CloudFormationPublisher publisher) {
        if (JsonType.NUMBER.equals(jsonType)) {
            return new JsonPrimitive(userResourceParamValue.getAsNumber());
        } else if (JsonType.COMMA_DELIMITED_LIST.equals(jsonType)){
            List<String> values = Arrays.asList(userResourceParamValue.getAsString().split(","));
            JsonArray jsonArray = new JsonArray(values.size());
            for (String value : values) {
                jsonArray.add(new JsonPrimitive(value));
            }
            return jsonArray;
        } else {
            String paramValue = userResourceParamValue.getAsString();
            if (paramValue.matches(RESOURCE_ID)) {
                paramValue = AwsKeyFinder.resourceId(paramValue, publisher);
            } else if (paramValue.matches(OUTPUT)){
                paramValue = AwsKeyFinder.output(paramValue, publisher);
            }
            return new JsonPrimitive(paramValue);
        }
    }

    private static JsonElement getSubJsonPrimitive(JsonElement userResourceParamValue, String userResourceParamName, JsonElement templateAttrValue) {
        return new JsonPrimitive(templateAttrValue.getAsString().replace("${".concat(userResourceParamName).concat("}"), userResourceParamValue.getAsString()));
    }

}
