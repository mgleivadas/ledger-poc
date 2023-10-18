package org.example.domain.ledger;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerAccountSnapshot(
      LedgerAccountAlias alias,
      Instant createdAt,
      BigDecimal balance
) {}
