package org.example.utils.exception.serialization;

public class SerializationException extends RuntimeException {

    public SerializationException(String message, Throwable nestedException) {
        super(message, nestedException);
    }
}
