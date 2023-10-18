package org.example.domain.ledger;


import java.time.Instant;

public record LedgerDetails(
      LedgerAlias alias,
      String title,
      Instant lastModifiedOn
) {
}
