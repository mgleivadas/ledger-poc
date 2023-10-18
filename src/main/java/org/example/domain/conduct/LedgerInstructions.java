package org.example.domain.conduct;

import org.example.domain.ledger.LedgerAccountAlias;

public final class LedgerInstructions {

    public sealed interface LedgerInstruction
          permits DebitInstruction,
          CreditInstruction,
          DoubleEntryInstruction {
    }

    public record DebitInstruction(LedgerAccountAlias ledgerAccountAlias) implements LedgerInstruction {
    }

    public record CreditInstruction(LedgerAccountAlias ledgerAccountAlias) implements LedgerInstruction {
    }

    public record DoubleEntryInstruction(
          LedgerAccountAlias debitLedgerAccountAlias,
          LedgerAccountAlias creditLedgerAccountAlias) implements LedgerInstruction {
    }
}
