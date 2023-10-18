package org.example.domain.ledger;

import static org.example.utils.ArgCheck.nonEmpty;

public record LedgerAccountAlias(String value) {

    public static LedgerAccountAlias of(String value) {
        return new LedgerAccountAlias(nonEmpty("LedgerAccountAlias", value));
    }
}
