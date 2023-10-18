package org.example.domain.definition;

import org.example.domain.ledger.LedgerAlias;
import org.example.error.InvalidBeginningOfLedgerException;
import org.example.parsing.LedgerDefinitionParser;
import org.example.parsing.LedgerDefinitionRequest;
import org.example.utils.DateUtils;
import org.example.utils.Either;

import java.util.ArrayList;
import java.util.List;

public final class LedgerDefinitionMapper {

    public static Either<List<String>, LedgerDefinition> of(String value) {
        return LedgerDefinitionParser.parse(value)
              .mapIfRight(parsedDefinition -> LedgerDefinitionMapper.map(value, parsedDefinition));
    }

    private static LedgerDefinition map(
          String ledgerDefinitionRequestStr,
          LedgerDefinitionRequest ledgerDefinitionRequest) {

        final List<LedgerAccountDefinition> ledgerAccountDefinitions = new ArrayList<>();
        final List<LedgerFlowDefinition> ledgerFlowDefinitions = new ArrayList<>();

        return new LedgerDefinition(
              ledgerDefinitionRequestStr,
              ledgerDefinitionRequest,
              LedgerAlias.of(ledgerDefinitionRequest.alias),
              ledgerDefinitionRequest.title,
              DateUtils.isValidInstant(ledgerDefinitionRequest.beginningOfLedger)
                    .orElseThrow(() -> new InvalidBeginningOfLedgerException(ledgerDefinitionRequest.beginningOfLedger)),
              ledgerDefinitionRequest.labels,
              ledgerAccountDefinitions,
              ledgerFlowDefinitions
        );
    }
}
