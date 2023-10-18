package org.example.domain.conduct;

import org.example.domain.conduct.LedgerInstructions.CreditInstruction;
import org.example.domain.conduct.LedgerInstructions.DebitInstruction;
import org.example.domain.conduct.LedgerInstructions.LedgerInstruction;
import org.example.domain.definition.LedgerDefinition;
import org.example.domain.definition.LedgerFlowDefinition;
import org.example.domain.journal.JournalEntryActions.DoubleEntryAction;
import org.example.domain.journal.JournalEntryActions.JournalEntryAction;
import org.example.domain.journal.JournalEntryActions.SingleEntryAction;
import org.example.domain.ledger.LedgerAccountAlias;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public final class LedgerRulesEngineFactory {

    public static LedgerRulesEngine create(LedgerDefinition definition) {

        return new LedgerRulesEngine(
              definition.beginningOfLedgerUtc(),
              createInstructionGenerators(definition.flowDefinitions()));
    }

    private static Map<String, LedgerDecisionGenerator> createInstructionGenerators(
          List<LedgerFlowDefinition> flowDefinitions) {

        return flowDefinitions
              .stream()
              .collect(toMap(LedgerFlowDefinition::getJournalEntryType, 
                    ledgerFlowDefinition -> constructLedgerDecisionGenerator(
                          ledgerFlowDefinition.getJournalEntryActions())));
    }

    private static LedgerDecisionGenerator constructLedgerDecisionGenerator(List<JournalEntryAction> actions) {

        return journalEntry -> {

            final List<LedgerInstruction> ledgerInstructions =
                  actions.stream()
                  .flatMap(action -> {
                      if (action instanceof SingleEntryAction sEntryAction) {
                          return switch (sEntryAction.txType()) {
                              case DEBIT -> Stream.of(new DebitInstruction(LedgerAccountAlias.of(sEntryAction.alias())));
                              case CREDIT -> Stream.of(new CreditInstruction(LedgerAccountAlias.of(sEntryAction.alias())));
                          };

                      } else if (action instanceof DoubleEntryAction dEntryAction) {

                          final LedgerInstruction firstInstruction = switch (dEntryAction.firstTxType()) {
                              case DEBIT -> new DebitInstruction(LedgerAccountAlias.of(dEntryAction.firstAlias()));
                              case CREDIT -> new CreditInstruction(LedgerAccountAlias.of(dEntryAction.firstAlias()));
                          };

                          final var secondInstruction = switch (dEntryAction.secondTxType()) {
                              case DEBIT -> new DebitInstruction(LedgerAccountAlias.of(dEntryAction.secondAlias()));
                              case CREDIT -> new CreditInstruction(LedgerAccountAlias.of(dEntryAction.secondAlias()));
                          };

                          return Stream.of(firstInstruction, secondInstruction);

                      } else {
                          throw new IllegalStateException();
                      }
                  })
                  .toList();

            // todo: Auditable entries need to be added here
            return new LedgerDecision(ledgerInstructions, List.of());
        };
    }
}
