package org.example.domain.ledger;


import java.time.Instant;

public record LedgerAccountDetails(
      LedgerAccountAlias alias,
      LedgerAlias ledgerAlias,
      String title,
      LedgerAccountType type,
      String currency,
      LedgerAccountSignage ledgerAccountSignage,
      Instant lastModifiedAt
) {

}
