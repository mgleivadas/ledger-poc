package org.example.repository;

import org.example.domain.definition.LedgerDefinition;
import org.example.domain.journal.JournalEntry;
import org.example.domain.ledger.LedgerAccountAlias;
import org.example.domain.ledger.LedgerAccountDetails;
import org.example.domain.ledger.LedgerAccountSnapshot;
import org.example.domain.ledger.LedgerAlias;
import org.example.domain.ledger.LedgerDetails;
import org.example.parsing.LedgerDefinitionRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LedgerRepository {

    List<LedgerDetails> fetchAllLedgers();

    Instant fetchLedgerLastModificationTimestamp(LedgerAlias ledgerAlias);

    Optional<LedgerAccountDetails> fetchLedgerAccountDetails(LedgerAlias ledgerAlias, LedgerAccountAlias accountAlias);

    Map<LedgerAccountAlias, LedgerAccountSnapshot> fetchLatestLedgerAccountSnapshots(LedgerAlias ledgerAlias);

    void saveLedgerAccountSnapshots(
          LedgerAlias ledgerAlias,
          Instant lastModificationOnLedger,
          List<LedgerAccountSnapshot> ledgerAccountSnapshots);

    void createNewLedger(LedgerDefinition ledgerDefinition);
    void deleteLedger(LedgerAlias ledgerAlias);

    Map<LedgerAlias, LedgerDefinitionRequest> fetchLedgerDefinitions(List<LedgerAlias> ledgerAliases);
}
