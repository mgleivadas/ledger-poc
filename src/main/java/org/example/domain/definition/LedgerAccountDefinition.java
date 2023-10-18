package org.example.domain.definition;

import org.example.domain.ledger.LedgerAccountAlias;
import org.example.domain.ledger.LedgerAccountCategory;
import org.example.domain.ledger.LedgerAccountSignage;
import org.example.domain.ledger.LedgerAccountType;

import java.math.BigDecimal;
import java.util.List;

public record LedgerAccountDefinition(
      LedgerAccountAlias ledgerAccountAlias,
      String title,
      LedgerAccountCategory ledgerAccountCategory,
      LedgerAccountType ledgerAccountType,
      LedgerAccountSignage ledgerAccountSignage,
      String currency,
      BigDecimal initialValue,
      List<String> labels
) {
}
