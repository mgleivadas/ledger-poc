package org.example.domain.processor;

import org.example.domain.event.Event;
import org.example.domain.journal.JournalEntry;
import org.example.repository.JournalEntryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JournalEntryProcessor implements Runnable {

    private final JournalEntryRepository journalEntryRepository;
    private volatile Optional<JournalEntry> lastSavedJournalEntry;

    public JournalEntryProcessor(
          JournalEntryRepository journalEntryRepository) {

        this.journalEntryRepository = journalEntryRepository;
        this.lastSavedJournalEntry = journalEntryRepository.fetchLastJournalEntry();
    }

    @Override
    public void run() {

        final List<JournalEntry> journalEntries = pollEventsFromEventsStore()
              .stream()
              .flatMap(this::mapEventToJournalEntry)
              .toList();

        if (!journalEntries.isEmpty()) {
            journalEntryRepository.save(journalEntries);
            lastSavedJournalEntry = Optional.of(journalEntries.get(journalEntries.size() - 1));
        }
    }

    public List<Event> pollEventsFromEventsStore() {
        return List.of();
    }

    public Optional<JournalEntry> getLastSavedJournalEntry() {
        return lastSavedJournalEntry;
    }

    private Stream<JournalEntry> mapEventToJournalEntry(Event event) {
        return Stream.of();
    }
}
