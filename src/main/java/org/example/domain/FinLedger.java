package org.example.domain;

import org.example.domain.definition.LedgerDefinition;
import org.example.domain.ledger.LedgerAlias;
import org.example.dto.LedgerOverviewDto;

import java.util.List;
import java.util.Optional;

public interface FinLedger {

    void start();

    void stop();

    boolean hasStopped();

    void createNewLedger(LedgerDefinition ledgerDefinition);
    void destroyLedger(LedgerAlias ledgerAlias);

    Optional<LedgerOverviewDto> getLedgerOverview(LedgerAlias ledgerAlias);

    List<LedgerAlias> listAllActiveLedgers();
}
