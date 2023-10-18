package org.example.domain.conduct;

public class InvalidLedgerInstruction extends RuntimeException {

    public InvalidLedgerInstruction(String message) {
        super("Reason:%s".formatted(message));
    }
}
