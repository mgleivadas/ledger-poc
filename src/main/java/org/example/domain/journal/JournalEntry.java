package org.example.domain.journal;

import org.example.domain.event.EventId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record JournalEntry(
      JournalEntryId id,
      EventId eventId,
      Instant businessTimestamp,
      String type,
      Map<String, String> payload
) {

    public Optional<BigDecimal> getFirstAmount() {
        return Optional.ofNullable(payload.get(FIRST_AMOUNT_KEY)).map(BigDecimal::new);
    }

    public Optional<BigDecimal> getSecondAmount() {
        return Optional.ofNullable(payload.get(SECOND_AMOUNT_KEY)).map(BigDecimal::new);
    }


    private static final String FIRST_AMOUNT_KEY = "FIRST_AMOUNT";
    private static final String SECOND_AMOUNT_KEY = "SECOND_AMOUNT";
}
