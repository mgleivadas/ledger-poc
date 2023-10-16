package org.example.utils.json.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.utils.exception.functional.ThrowingSupplier;
import org.example.utils.exception.serialization.SerializationException;

public abstract class JsonNode {

    public JsonNode(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public abstract Object getTopContainerNode();

    public String toJson() {
         return wrapToSerializationException(() -> objectMapper.writeValueAsString(getTopContainerNode()));
    }

    public com.fasterxml.jackson.databind.JsonNode innerNode() {
        return (com.fasterxml.jackson.databind.JsonNode) getTopContainerNode();
    }

    private <T> T wrapToSerializationException(ThrowingSupplier<T> mapper) {
        try {
            return mapper.get();
        } catch (Throwable ex) {
            throw new SerializationException("Serialization exception", ex);
        }
    }

    private final ObjectMapper objectMapper;
}
