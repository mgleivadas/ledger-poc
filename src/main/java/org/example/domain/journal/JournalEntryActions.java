package org.example.domain.journal;

import org.example.domain.ledger.FinancialTransactionType;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.example.domain.ledger.FinancialTransactionType.CREDIT;
import static org.example.domain.ledger.FinancialTransactionType.DEBIT;

public final class JournalEntryActions {

    public static Optional<SingleEntryAction> tryParseSingleEntryAction(String value) {

        final var matcher = SINGLE_ENTRY_ACTION_PATTERN.matcher(value);

        if (matcher.matches() && nonNull(matcher.group(1)) && nonNull(matcher.group(2))) {
            final var txType = matcher.group(1).equals("D") ? DEBIT : CREDIT;
            return Optional.of(new SingleEntryAction(txType, matcher.group(2)));
        } else {
            return empty();
        }
    }

    public static Optional<DoubleEntryAction> tryParseDoubleEntryAction(String value) {

        final var matcher = DOUBLE_ENTRY_ACTION_PATTERN.matcher(value);

        if (matcher.matches() && nonNull(matcher.group(1)) && nonNull(matcher.group(2)) && nonNull(matcher.group(3))) {
            return Optional.of(new DoubleEntryAction(CREDIT, matcher.group(2), DEBIT, matcher.group(3)));
        } else if (matcher.matches() && nonNull(matcher.group(4)) && nonNull(matcher.group(5)) && nonNull(matcher.group(6))) {
            return Optional.of(new DoubleEntryAction(DEBIT, matcher.group(5), CREDIT, matcher.group(6)));
        } else {
            return empty();
        }
    }

    public sealed interface JournalEntryAction permits DoubleEntryAction, SingleEntryAction {}

    public record DoubleEntryAction(
          FinancialTransactionType firstTxType, String firstAlias,
          FinancialTransactionType secondTxType, String secondAlias) implements JournalEntryAction {}

    public record SingleEntryAction(FinancialTransactionType txType, String alias) implements JournalEntryAction {}

    private static final Pattern SINGLE_ENTRY_ACTION_PATTERN = Pattern.compile("^([C|D])\\[([A-Z0-9_]+)]$");
    private static final Pattern DOUBLE_ENTRY_ACTION_PATTERN =
          Pattern.compile("(^C\\[([A-Z0-9_]+)]_D\\[([A-Z0-9_]+)]$)|(^D\\[([A-Z0-9_]+)]_C\\[([A-Z0-9_]+)]$)");
}
