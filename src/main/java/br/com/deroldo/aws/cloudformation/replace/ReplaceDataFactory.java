package br.com.deroldo.aws.cloudformation.replace;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Optional;

public class ReplaceDataFactory {

    public static Optional<ReplaceData> create(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue, boolean isNumber) {
        Optional<JsonElement> element = Optional.empty();
        if (isPrimitiveValidToReplace(userResourceParamName, userResourceParamValue, templateAttr, templateAttrValue)) {
            element = Optional.of(getJsonPrimitive(userResourceParamValue, isNumber));
        } else if (isMapValidToReplace(userResourceParamName, templateAttr, templateAttrValue)) {
            element = Optional.of(userResourceParamValue);
        }
        return element.map(ReplaceData::new);
    }

    private static boolean isPrimitiveValidToReplace(String userResourceParamName, JsonElement userResourceParamValue, String templateAttr, JsonElement templateAttrValue) {
        return userResourceParamValue.isJsonPrimitive() && "Ref".equals(templateAttr) && templateAttrValue.getAsString().equals(userResourceParamName);
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
