package org.example.domain.journal;

import java.util.UUID;

public record JournalEntryId(UUID value) {

    public static JournalEntryId createNew() {
        return new JournalEntryId(UUID.randomUUID());
    }
}
