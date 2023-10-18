package org.example.error;

import org.example.domain.ledger.LedgerAlias;
import java.util.List;

public final class InvalidLedgerDefinition extends RuntimeException {

    public InvalidLedgerDefinition(LedgerAlias ledgerAlias, List<String> errors) {
        super("""
              Ledger conduct could not be created for ledger: '%s' based on the definition file stored.
              Errors: %s
              """.formatted(ledgerAlias, String.join(",", errors)));
    }
}
