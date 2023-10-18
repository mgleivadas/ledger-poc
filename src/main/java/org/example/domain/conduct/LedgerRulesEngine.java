package org.example.domain.conduct;

import org.example.domain.journal.JournalEntry;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class LedgerRulesEngine {

    private final Instant beginningOfLedger;
    private final Map<String, LedgerDecisionGenerator> decisionGenerators;

    public LedgerRulesEngine(Instant beginningOfLedger, Map<String, LedgerDecisionGenerator> decisionGenerators) {
        this.beginningOfLedger = beginningOfLedger;
        this.decisionGenerators = decisionGenerators;
    }

    public LedgerDecision consult(JournalEntry journalEntry) {

        if (beginningOfLedger.isBefore(journalEntry.businessTimestamp())) {
            return new LedgerDecision(List.of(), List.of(AuditableEntry.ignoreDated(journalEntry.id())));
        }

        if (!decisionGenerators.containsKey(journalEntry.type())) {
            return new LedgerDecision(List.of(), List.of(AuditableEntry.ignoreNoMapping(journalEntry.id())));
        }

        return decisionGenerators.get(journalEntry.type()).generate(journalEntry);
    }
}
