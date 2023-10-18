package org.example.repository;

import org.example.domain.definition.LedgerAccountDefinition;
import org.example.domain.definition.LedgerDefinition;
import org.example.domain.journal.JournalEntry;
import org.example.domain.ledger.LedgerAccountAlias;
import org.example.domain.ledger.LedgerAccountDetails;
import org.example.domain.ledger.LedgerAccountSnapshot;
import org.example.domain.ledger.LedgerAlias;
import org.example.domain.ledger.LedgerDetails;
import org.example.parsing.LedgerDefinitionRequest;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public final class InMemoryGenericRepository
      implements JournalEntryRepository, LedgerRepository {

    public static final InMemoryGenericRepository INSTANCE = new InMemoryGenericRepository();

    private final ArrayDeque<JournalEntry> journalEntries;
    private final Map<LedgerAlias, LedgerState> ledgerState;


    public InMemoryGenericRepository() {
        this.journalEntries = new ArrayDeque<>();
        this.ledgerState = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void save(List<JournalEntry> newJournalEntries) {
        journalEntries.addAll(newJournalEntries);
    }

    @Override
    public void saveAndTriggerReposting(List<JournalEntry> journalEntries) {

    }

    @Override
    public Optional<JournalEntry> fetchLastJournalEntry() {
        return journalEntries.isEmpty() ? Optional.empty() : Optional.of(journalEntries.getLast());
    }

    @Override
    public List<JournalEntry> fetchUnPostedJournalEntries(Instant fromTimestamp) {
        return null;
    }

    @Override
    public List<LedgerDetails> fetchAllLedgers() {
        return ledgerState.values().stream().map(ld -> ld.ledgerDetails).toList();
    }

    @Override
    public Optional<LedgerAccountDetails> fetchLedgerAccountDetails(
          LedgerAlias ledgerAlias, LedgerAccountAlias accountAlias) {

        return Optional.ofNullable(ledgerState.get(ledgerAlias).ledgerAccountDetailsMap.get(accountAlias));
    }

    @Override
    public Instant fetchLedgerLastModificationTimestamp(LedgerAlias ledgerAlias) {
        return ledgerState.get(ledgerAlias)
              .ledgerDetails
              .lastModifiedOn();
    }

    @Override
    public Map<LedgerAccountAlias, LedgerAccountSnapshot> fetchLatestLedgerAccountSnapshots(LedgerAlias ledgerAlias) {
        return ledgerState.get(ledgerAlias)
              .ledgerAccountSnapshots
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(e.getValue().size() - 1)));
    }

    /**
     * Needs to atomically update LedgerDetails#lastModifiedOn and add newLedgerAccountSnapshots
     * in the final db version
     */
    @Override
    public void saveLedgerAccountSnapshots(
          LedgerAlias ledgerAlias,
          Instant lastModificationOnLedger,
          List<LedgerAccountSnapshot> newLedgerAccountSnapshots) {

        final var ledgerState = this.ledgerState.get(ledgerAlias);

        ledgerState.ledgerDetails = new LedgerDetails(
              ledgerState.ledgerDetails.alias(),
              ledgerState.ledgerDetails.title(),
              lastModificationOnLedger);

        newLedgerAccountSnapshots.forEach(newLedgerAccountSnapshot -> {
            ledgerState.ledgerAccountSnapshots.get(newLedgerAccountSnapshot.alias()).add(newLedgerAccountSnapshot);
        });
    }

    @Override
    public synchronized void createNewLedger(LedgerDefinition ledgerDefinition) {

        final LedgerDetails ledgerDetails = new LedgerDetails(
              ledgerDefinition.ledgerAlias(),
              ledgerDefinition.title(),
              ledgerDefinition.beginningOfLedgerUtc());

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

        final Map<LedgerAccountAlias, List<LedgerAccountSnapshot>> ledgerAccountSnapshots =
              ledgerDefinition.ledgerAccountDefinitions()
              .stream()
              .collect(toMap(LedgerAccountDefinition::ledgerAccountAlias, lad -> {
                  final List<LedgerAccountSnapshot> snapShotList = new ArrayList<>();
                  snapShotList.add(
                        new LedgerAccountSnapshot(
                              lad.ledgerAccountAlias(),
                              ledgerDefinition.beginningOfLedgerUtc(),
                              lad.initialValue()));
                  return snapShotList;
                }));

        final LedgerState newLedgerstate = new LedgerState(
              ledgerDefinition.ledgerDefinitionRequest(),
              ledgerDetails,
              ledgerAccountDetailsMap,
              ledgerAccountSnapshots);

        ledgerState.put(ledgerDefinition.ledgerAlias(), newLedgerstate);
    }

    @Override
    public synchronized void deleteLedger(LedgerAlias ledgerAlias) {
        ledgerState.remove(ledgerAlias);
    }

    @Override
    public Map<LedgerAlias, LedgerDefinitionRequest> fetchLedgerDefinitions(List<LedgerAlias> ledgerAliases) {
        return ledgerState.entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, e -> e.getValue().ledgerDefinitionRequest));
    }

    private final class LedgerState {

        final LedgerDefinitionRequest ledgerDefinitionRequest;
        volatile LedgerDetails ledgerDetails;
        final Map<LedgerAccountAlias, LedgerAccountDetails> ledgerAccountDetailsMap;
        final Map<LedgerAccountAlias, List<LedgerAccountSnapshot>> ledgerAccountSnapshots;

        public LedgerState(
              LedgerDefinitionRequest ledgerDefinitionRequest,
              LedgerDetails ledgerDetails,
              Map<LedgerAccountAlias, LedgerAccountDetails> ledgerAccountDetailsMap,
              Map<LedgerAccountAlias, List<LedgerAccountSnapshot>> ledgerAccountSnapshots) {

            this.ledgerDefinitionRequest = ledgerDefinitionRequest;
            this.ledgerDetails = ledgerDetails;
            this.ledgerAccountDetailsMap = ledgerAccountDetailsMap;
            this.ledgerAccountSnapshots = ledgerAccountSnapshots;
        }
    }
}
