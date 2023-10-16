package org.example.utils.json;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonConfiguration {
    public final static ObjectMapper OBJECT_MAPPER = defaultConfiguration();

    private JsonConfiguration() {
    }

    private static ObjectMapper defaultConfiguration() {
        return new ObjectMapper();
    }
}
