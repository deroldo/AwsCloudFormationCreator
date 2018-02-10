package br.com.deroldo.aws.cloudformation.replace;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public class ReplaceDataFactory {

    public static Optional<ReplaceData> create(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue, boolean isNumber) {
        Optional<JsonElement> element = Optional.empty();
        if (isPrimitiveValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)) {
            element = Optional.of(getJsonPrimitive(userResourceParamValue, isNumber));
        } else if (isArrayValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)) {
            element = Optional.of(userResourceParamValue.getAsJsonArray());
        } else if (isObjectValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)) {
            element = Optional.of(userResourceParamValue.getAsJsonObject());
        } else if (isMapValidToReplace(userResourceParamName, templateAttr, templateAttrValue)) {
            element = Optional.of(userResourceParamValue);
        }
        return element.map(ReplaceData::new);
    }

    private static boolean isPrimitiveValidToReplace(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonPrimitive() && "Ref".equals(templateAttr)
                && templateAttrValue.isJsonPrimitive() && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static boolean isArrayValidToReplace(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonArray() && templateAttr.equals("Ref") && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static boolean isObjectValidToReplace(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonObject() && templateAttr.equals("Ref") && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static boolean isMapValidToReplace(String userResourceParamName, String templateAttr, JsonElement templateAttrValue) {
        return templateAttr.equals("Fn::FindInMap") && templateAttrValue.getAsString().equals(userResourceParamName);
    }

    private static JsonElement getJsonPrimitive(final JsonElement userResourceParamValue, final boolean isNumber) {
        if (isNumber) {
            return new JsonPrimitive(userResourceParamValue.getAsNumber());
        } else {
            return new JsonPrimitive(userResourceParamValue.getAsString());
        }
    }

}
