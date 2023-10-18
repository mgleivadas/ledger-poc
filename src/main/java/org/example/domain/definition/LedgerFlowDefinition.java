package org.example.domain.definition;

import org.example.domain.journal.JournalEntryActions.JournalEntryAction;
import java.util.List;

public final class LedgerFlowDefinition {

    private final String groupingFlowName;
    private final String journalEntryType;
    private List<JournalEntryAction> journalEntryActions;

    public String getGroupingFlowName() {
        return groupingFlowName;
    }

    public String getJournalEntryType() {
        return journalEntryType;
    }

    public List<JournalEntryAction> getJournalEntryActions() {
        return journalEntryActions;
    }

    public LedgerFlowDefinition(
          String groupingFlowName,
          String journalEntryType,
          List<JournalEntryAction> journalEntryActions) {

        this.groupingFlowName = groupingFlowName;
        this.journalEntryType = journalEntryType;
        this.journalEntryActions = journalEntryActions;
    }
}
