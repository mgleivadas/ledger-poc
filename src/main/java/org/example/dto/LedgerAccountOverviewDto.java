package org.example.dto;

import org.example.domain.ledger.LedgerAccountAlias;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerAccountOverviewDto(
      LedgerAccountAlias alias,
      String title,
      Instant lastModifiedOn,
      BigDecimal balance
) {
}
