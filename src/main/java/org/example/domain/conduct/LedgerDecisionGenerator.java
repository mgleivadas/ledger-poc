package org.example.domain.conduct;
import org.example.domain.journal.JournalEntry;

@FunctionalInterface
public interface LedgerDecisionGenerator {
    LedgerDecision generate(JournalEntry journalEntry);
}
