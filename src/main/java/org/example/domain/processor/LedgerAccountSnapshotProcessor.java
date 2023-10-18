package org.example.domain.processor;

import org.example.domain.conduct.LedgerInstructions.CreditInstruction;
import org.example.domain.conduct.LedgerInstructions.DebitInstruction;
import org.example.domain.conduct.LedgerInstructions.DoubleEntryInstruction;
import org.example.domain.conduct.LedgerInstructions.LedgerInstruction;
import org.example.domain.conduct.LedgerRulesEngine;
import org.example.domain.conduct.LedgerDecision;
import org.example.domain.journal.JournalEntry;
import org.example.domain.ledger.LedgerAccountAlias;
import org.example.domain.ledger.LedgerAccountDetails;
import org.example.domain.ledger.LedgerAccountSnapshot;

import java.util.List;
import java.util.Map;

import static org.example.domain.ledger.LedgerAccountSignage.POSITIVE_CREDIT_NEGATIVE_DEBIT;
import static org.example.domain.ledger.LedgerAccountSignage.POSITIVE_DEBIT_NEGATIVE_CREDIT;

public final class LedgerAccountSnapshotProcessor {

    private final LedgerRulesEngine ledgerRulesEngine;
    private final Map<LedgerAccountAlias, LedgerAccountDetails> ledgerAccountDetailsMap;
    private final Map<LedgerAccountAlias, LedgerAccountSnapshot> currentSnapshots;

    public LedgerAccountSnapshotProcessor(
          LedgerRulesEngine ledgerRulesEngine,
          Map<LedgerAccountAlias, LedgerAccountDetails> ledgerAccountDetailsMap,
          Map<LedgerAccountAlias, LedgerAccountSnapshot> currentSnapshots) {

        this.ledgerRulesEngine = ledgerRulesEngine;
        this.ledgerAccountDetailsMap = ledgerAccountDetailsMap;
        this.currentSnapshots = currentSnapshots;
    }
    
    public void updateLedgerAccountSnapshot(List<LedgerAccountSnapshot> newSnapshots) {
        newSnapshots.forEach(snapshot -> currentSnapshots.put(snapshot.alias(), snapshot));
    }

    public Map<LedgerAccountAlias, LedgerAccountSnapshot> getCurrentSnapshots() {
        return currentSnapshots;
    }

    public List<LedgerAccountSnapshot> calculateNewSnapshots(List<JournalEntry> journalEntries) {

        return journalEntries
              .stream()
              .flatMap(journalEntry -> {
                  final LedgerDecision decision = ledgerRulesEngine.consult(journalEntry);

                  return decision.ledgerInstructions()
                        .stream()
                        .flatMap(ledgerInstruction -> applyInstruction(journalEntry, ledgerInstruction).stream());
              })
              .toList();
    }

    private List<LedgerAccountSnapshot> applyInstruction(JournalEntry jentry, LedgerInstruction ledgerInstruction) {

        if (ledgerInstruction instanceof CreditInstruction creditInstruction) {
            final var currSnapshot = currentSnapshots.get(creditInstruction.ledgerAccountAlias());
            final var ledgerAccountDetails = ledgerAccountDetailsMap.get(creditInstruction.ledgerAccountAlias());

            final var amount = ledgerAccountDetails.ledgerAccountSignage() == POSITIVE_CREDIT_NEGATIVE_DEBIT
                  ? jentry.getFirstAmount().orElseThrow().abs()
                  : jentry.getFirstAmount().orElseThrow().abs().negate();

            return List.of(new LedgerAccountSnapshot(
                  creditInstruction.ledgerAccountAlias(),
                  jentry.businessTimestamp(),
                  currSnapshot.balance().add(amount)));

        } else if (ledgerInstruction instanceof DebitInstruction debitInstruction) {
            final var currSnapshot = currentSnapshots.get(debitInstruction.ledgerAccountAlias());
            final var ledgerAccountDetails = ledgerAccountDetailsMap.get(debitInstruction.ledgerAccountAlias());

            final var amount = ledgerAccountDetails.ledgerAccountSignage() == POSITIVE_DEBIT_NEGATIVE_CREDIT
                  ? jentry.getFirstAmount().orElseThrow().abs()
                  : jentry.getFirstAmount().orElseThrow().abs().negate();

            return List.of(new LedgerAccountSnapshot(
                  debitInstruction.ledgerAccountAlias(),
                  jentry.businessTimestamp(),
                  currSnapshot.balance().add(amount)));

        } else if(ledgerInstruction instanceof DoubleEntryInstruction doubleEntryInstruction) {

            final var currSnapshotCreditLeg = currentSnapshots.get(doubleEntryInstruction.creditLedgerAccountAlias());
            final var currSnapshotDebitLeg = currentSnapshots.get(doubleEntryInstruction.debitLedgerAccountAlias());

            final var ledgerAccountDetailsCreditLeg =
                  ledgerAccountDetailsMap.get(doubleEntryInstruction.creditLedgerAccountAlias());
            final var ledgerAccountDetailsDebitLeg =
                  ledgerAccountDetailsMap.get(doubleEntryInstruction.debitLedgerAccountAlias());

            final var amountCreditLeg = ledgerAccountDetailsCreditLeg.ledgerAccountSignage() == POSITIVE_CREDIT_NEGATIVE_DEBIT
                  ? jentry.getFirstAmount().orElseThrow().abs()
                  : jentry.getFirstAmount().orElseThrow().abs().negate();

            final var amountDebitLeg = ledgerAccountDetailsDebitLeg.ledgerAccountSignage() == POSITIVE_DEBIT_NEGATIVE_CREDIT
                  ? jentry.getSecondAmount().orElseThrow().abs()
                  : jentry.getSecondAmount().orElseThrow().abs().negate();

            return List.of(
                  new LedgerAccountSnapshot(
                        doubleEntryInstruction.creditLedgerAccountAlias(),
                        jentry.businessTimestamp(),
                        currSnapshotCreditLeg.balance().add(amountCreditLeg)),
                  new LedgerAccountSnapshot(
                        doubleEntryInstruction.debitLedgerAccountAlias(),
                        jentry.businessTimestamp(),
                        currSnapshotDebitLeg.balance().add(amountDebitLeg)));
        } else {
            throw new RuntimeException("Unknown ledgerInstruction");
        }
    }

    // Java 21
//    private List<LedgerAccountSnapshot> applyInstruction(JournalEntry jentry, LedgerInstruction ledgerInstruction) {
//
//        return switch (ledgerInstruction) {
//            case CreditInstruction creditInstruction -> {
//                final var currSnapshot = currentSnapshots.get(creditInstruction.credit().getLedgerAccountId());
//                yield List.of(calculateNewSnapshot(currSnapshot, jentry, creditInstruction.credit().getCorrectedTxAmount()));
//            }
//            case DebitInstruction debitInstruction -> {
//                final var currSnapshot = currentSnapshots.get(debitInstruction.debit().getLedgerAccountId());
//                yield List.of(calculateNewSnapshot(currSnapshot, jentry, debitInstruction.debit().getCorrectedTxAmount()));
//            }
//            case DoubleEntryInstruction doubleEntryInstruction -> {
//
//                final var currSnapshotToBeDebited = currentSnapshots.get(doubleEntryInstruction.debit().getLedgerAccountId());
//                final var currSnapshotToBeCredited = currentSnapshots.get(doubleEntryInstruction.credit().getLedgerAccountId());
//
//                yield List.of(
//                      calculateNewSnapshot(currSnapshotToBeDebited, jentry, doubleEntryInstruction.debit().getCorrectedTxAmount()),
//                      calculateNewSnapshot(currSnapshotToBeCredited, jentry, doubleEntryInstruction.credit().getCorrectedTxAmount())
//                );
//            }
//        };
//    }
}
