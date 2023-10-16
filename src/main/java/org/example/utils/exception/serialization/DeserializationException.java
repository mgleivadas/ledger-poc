package org.example.utils.exception.serialization;

public class DeserializationException extends RuntimeException {

    public DeserializationException(String message, Throwable nestedException) {
        super(message, nestedException);
    }
}
