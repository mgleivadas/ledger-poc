package org.example.domain.conduct;


import org.example.domain.journal.JournalEntryId;

public record AuditableEntry(JournalEntryId journalEntryId, String message) {

    private AuditableEntry(JournalEntryId journalEntryId, IgnoringReason ignoringReason) {
        this(journalEntryId, ignoringReason.name());
    }

    public static AuditableEntry ignoreNoMapping(JournalEntryId journalEntryId) {
        return new AuditableEntry(journalEntryId, IgnoringReason.NO_MAPPING);
    }

    public static AuditableEntry ignoreDated(JournalEntryId journalEntryId) {
        return new AuditableEntry(journalEntryId, IgnoringReason.DATED);
    }

    public String get() {
        return "[%s]: %s".formatted(journalEntryId.value().toString(), message);
    }

    enum IgnoringReason {
        NO_MAPPING,
        DATED
    }
}
