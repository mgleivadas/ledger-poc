package org.example.repository;

import org.example.domain.journal.JournalEntry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository {

    void save(List<JournalEntry> journalEntries);

    void saveAndTriggerReposting(List<JournalEntry> journalEntries);

    Optional<JournalEntry> fetchLastJournalEntry();

    /**
     * Fetch journal entries which are AFTER the argument given.
     */
    List<JournalEntry> fetchUnPostedJournalEntries(Instant fromTimestamp);
}
