package org.example.domain.definition;

import org.example.domain.ledger.LedgerAlias;
import org.example.parsing.LedgerDefinitionRequest;

import java.time.Instant;
import java.util.List;

public record LedgerDefinition(
      String ledgerDefinitionRequestStr,
      LedgerDefinitionRequest ledgerDefinitionRequest,
      LedgerAlias ledgerAlias,
      String title,
      Instant beginningOfLedgerUtc,
      List<String> labels,
      List<LedgerAccountDefinition> ledgerAccountDefinitions,
      List<LedgerFlowDefinition> flowDefinitions
) {

}
