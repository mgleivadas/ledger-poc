package org.example.domain.ledger;

import org.example.domain.conduct.LedgerRulesEngine;
import org.example.domain.conduct.LedgerRulesEngineFactory;
import org.example.domain.definition.LedgerDefinitionMapper;
import org.example.domain.journal.JournalEntry;
import org.example.domain.processor.JournalEntryProcessor;
import org.example.domain.processor.LedgerAccountSnapshotProcessor;
import org.example.error.InvalidLedgerDefinition;
import org.example.repository.InMemoryGenericRepository;
import org.example.repository.JournalEntryRepository;
import org.example.repository.LedgerRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public final class LedgerActor implements Runnable {

    private final JournalEntryProcessor journalEntryProcessor;
    private final LedgerAlias ledgerAlias;
    private final LedgerRepository ledgerRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountSnapshotProcessor ledgerAccountSnapshotProcessor;

    public LedgerActor(
          JournalEntryProcessor journalEntryProcessor,
          LedgerAlias ledgerAlias,
          String ledgerDefinitionStr) {

        this.journalEntryProcessor = journalEntryProcessor;
        this.ledgerAlias = ledgerAlias;

        final var ledgerDefinition = LedgerDefinitionMapper.of(ledgerDefinitionStr)
              .getRightOrElseThrow(errors -> new InvalidLedgerDefinition(ledgerAlias, errors));

        this.ledgerRepository = InMemoryGenericRepository.INSTANCE;
        this.journalEntryRepository = InMemoryGenericRepository.INSTANCE;

        final Map<LedgerAccountAlias, LedgerAccountDetails> ledgerAccountDetailsMap =
              ledgerDefinition.ledgerAccountDefinitions()
              .stream()
              .map(lad -> new LedgerAccountDetails(
                    lad.ledgerAccountAlias(),
                    ledgerDefinition.ledgerAlias(),
                    lad.title(),
                    lad.ledgerAccountType(),
                    lad.currency(),
                    lad.ledgerAccountSignage(),
                    ledgerDefinition.beginningOfLedgerUtc()))
              .collect(toMap(LedgerAccountDetails::alias, Function.identity()));

        this.ledgerAccountSnapshotProcessor = new LedgerAccountSnapshotProcessor(
              LedgerRulesEngineFactory.create(ledgerDefinition),
              ledgerAccountDetailsMap,
              ledgerRepository.fetchLatestLedgerAccountSnapshots(ledgerAlias));
    }

    @Override
    public void run() {
        // checkAndHandleRepostIncidents();
        runLedgerPostingProcess();
    }

    private void runLedgerPostingProcess() {

        journalEntryProcessor.getLastSavedJournalEntry().ifPresent(lastEntrySaved -> {
            final var ledgerLastModificationTimestamp =
                  ledgerRepository.fetchLedgerLastModificationTimestamp(ledgerAlias);

            if (lastEntrySaved.businessTimestamp().isAfter(ledgerLastModificationTimestamp)) {

                final List<JournalEntry> unPostedEntries =
                      journalEntryRepository.fetchUnPostedJournalEntries(ledgerLastModificationTimestamp);

                saveNewJournalEntries(unPostedEntries);
            }
        });
    }

    private void saveNewJournalEntries(List<JournalEntry> journalEntriesToProcess) {
        // todo: List::getLast() - Java 21
        final var lastJournalEntry = journalEntriesToProcess.get(journalEntriesToProcess.size() - 1);
        final var newSnapshots = ledgerAccountSnapshotProcessor.calculateNewSnapshots(journalEntriesToProcess);
        ledgerRepository.saveLedgerAccountSnapshots(ledgerAlias, lastJournalEntry.businessTimestamp(), newSnapshots);
        ledgerAccountSnapshotProcessor.updateLedgerAccountSnapshot(newSnapshots);
    }
}
