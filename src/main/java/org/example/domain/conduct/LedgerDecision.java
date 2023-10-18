package org.example.domain.conduct;


import org.example.domain.conduct.LedgerInstructions.LedgerInstruction;
import java.util.List;

public record LedgerDecision(List<LedgerInstruction> ledgerInstructions, List<AuditableEntry> auditableEntries) {}
