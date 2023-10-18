package org.example.dto;

import org.example.domain.ledger.LedgerAccountAlias;
import org.example.domain.ledger.LedgerAlias;

import java.util.Map;

public record LedgerOverviewDto(
      LedgerAlias ledgerAlias,
      Map<LedgerAccountAlias, LedgerAccountOverviewDto> ledgerAccountOverviews
) {
}
