package org.example.error;

public final class InvalidBeginningOfLedgerException extends RuntimeException {
    public InvalidBeginningOfLedgerException(String value) {
        super("Invalid beginning of ledger value: '%s'".formatted(value));
    }
}
