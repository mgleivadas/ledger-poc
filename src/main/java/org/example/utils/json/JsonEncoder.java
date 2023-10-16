package org.example.utils.json;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.utils.json.core.JsonArray;
import org.example.utils.json.core.JsonObject;

import static org.example.utils.json.JsonConfiguration.OBJECT_MAPPER;


public final class JsonEncoder {

    public static JsonObject newObject() {
        return JsonObject.with(OBJECT_MAPPER);
    }

    public static JsonArray newArray() {
        return JsonArray.with(OBJECT_MAPPER);
    }

    public static <T> String serialize(T object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
