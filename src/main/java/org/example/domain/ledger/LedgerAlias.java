package org.example.domain.ledger;

import static org.example.utils.ArgCheck.nonEmpty;

public record LedgerAlias(String value) {

    public static LedgerAlias of(String value) {
        return new LedgerAlias(nonEmpty("LedgerAlias", value));
    }
}
