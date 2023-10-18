package org.example.domain;

import org.example.domain.definition.LedgerDefinition;
import org.example.domain.ledger.LedgerActor;
import org.example.domain.ledger.LedgerAlias;
import org.example.domain.processor.JournalEntryProcessor;
import org.example.dto.LedgerOverviewDto;
import org.example.repository.JournalEntryRepository;
import org.example.repository.LedgerRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SingleInstanceFinLedger implements FinLedger {

    private final JournalEntryProcessor journalEntryProcessor;
    private final LedgerRepository ledgerRepository;
    private final Map<LedgerAlias, LedgerActor> ledgerActors;

    public SingleInstanceFinLedger(
          JournalEntryRepository journalEntryRepository,
          LedgerRepository ledgerRepository) {

        this.journalEntryProcessor = new JournalEntryProcessor(journalEntryRepository);
        this.ledgerRepository = ledgerRepository;
        this.ledgerActors = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        while (true) {
            try {
                ledgerActors.values().forEach(LedgerActor::run);
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean hasStopped() {
        return false;
    }

    @Override
    public synchronized void createNewLedger(LedgerDefinition ledgerDefinition) {

        if (!ledgerActors.containsKey(ledgerDefinition.ledgerAlias())) {

            ledgerRepository.createNewLedger(ledgerDefinition);
            ledgerActors.put(ledgerDefinition.ledgerAlias(),
                  new LedgerActor(
                        journalEntryProcessor,
                        ledgerDefinition.ledgerAlias(),
                        ledgerDefinition.ledgerDefinitionRequestStr()));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public synchronized void destroyLedger(LedgerAlias ledgerAlias) {
        ledgerActors.remove(ledgerAlias);
        ledgerRepository.deleteLedger(ledgerAlias);
    }

    @Override
    public Optional<LedgerOverviewDto> getLedgerOverview(LedgerAlias ledgerAlias) {
        return Optional.empty();
    }

    @Override
    public List<LedgerAlias> listAllActiveLedgers() {
        return null;
    }


//    private void createAllLedgerActorInstances() {
//        final var allLedgers = ledgerRepository.fetchAllLedgers();
//
//        final var allLedgerAliases = ledgerRepository.fetchAllLedgers().stream().map(LedgerDetails::alias).toList();
//
//        final var allLedgerDefinitions = ledgerDefinitionRepository.fetchLedgerDefinitions(allLedgerAliases);
//
//
//    }
}
