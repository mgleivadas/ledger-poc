package org.example.parsing;

import org.example.domain.journal.JournalEntryActions;
import org.example.parsing.LedgerDefinitionRequest.LedgerAccountDefinitionRequest;
import org.example.utils.ArgCheck;
import org.example.utils.DateUtils;
import org.example.utils.Either;
import org.example.utils.json.JsonDecoder;
import org.example.utils.validation.ShortCircuitValidator;
import org.example.utils.validation.ShortCircuitValidator.ValidationContext;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class LedgerDefinitionParser {

    public static Either<List<String>, LedgerDefinitionRequest> parse(String ledgerDefinitionRequest) {

        final var validationResult =
              ShortCircuitValidator.of(ledgerDefinitionRequest)
                    .addValidationBlock(vc -> vc.ifEmpty(vc.getVal(), "Ledger definition is empty."))
                    .addTransformationBlock(value -> JsonDecoder.to(value, LedgerDefinitionRequest.class))
                    .addValidationBlock(LedgerDefinitionParser::validateTopLevelFields)
                    .addValidationBlock(LedgerDefinitionParser::validateBeginningOfLedgerValue)
                    .addValidationBlock(LedgerDefinitionParser::validateNoDuplicateLedgerAccountAliasExist)
                    .addValidationBlock(LedgerDefinitionParser::validateNoDuplicateJournalEntriesExist)
                    .addValidationBlock(LedgerDefinitionParser::validateJournalEntryActions)
                    .addValidationBlock(LedgerDefinitionParser::validateNoDuplicateLedgerLabelsExist)
                    .addValidationBlock(LedgerDefinitionParser::validateNoDuplicateLedgerAccountLabelsExist)
                    .tryValidate();

        if (validationResult.hasFailed()) {
            if (validationResult.exceptionOpt().isPresent()) {
                return Either.left(List.of(validationResult.exceptionOpt().get().getMessage()));
            } else {
                return Either.left(validationResult.errors());
            }
        } else {
            return Either.right(validationResult.getOrThrow());
        }
    }

    static void validateTopLevelFields(ValidationContext<LedgerDefinitionRequest> validationContext) {
        final var parsedLedger = validationContext.getVal();

        validationContext.ifNull(parsedLedger.alias, missingField("alias"));
        validationContext.ifNull(parsedLedger.beginningOfLedger, missingField("beginning_of_ledger_utc"));
        validationContext.ifNull(parsedLedger.title, missingField("title"));
        validationContext.ifNull(parsedLedger.labels, missingField("labels"));
        validationContext.ifNull(parsedLedger.ledgerAccounts, missingField("ledger_accounts"));
        validationContext.ifNull(parsedLedger.flowsRequest, missingField("flows"));
    }

    static void validateNoDuplicateLedgerAccountAliasExist(ValidationContext<LedgerDefinitionRequest> validationContext) {
        final var ledgerAccountsNames = validationContext.getVal().ledgerAccounts
              .stream()
              .map(LedgerAccountDefinitionRequest::alias)
              .toList();

        validationContext.ifDuplicateValuesExist(ledgerAccountsNames, duplicates ->
            validationContext.error("Duplicate ledger account aliases detected: [%s]"
                  .formatted(String.join(",", duplicates)))
        );
    }

    static void validateNoDuplicateJournalEntriesExist(ValidationContext<LedgerDefinitionRequest> validationContext) {

         final var journalEntryTypes = validationContext.getVal().flowsRequest.getAll()
               .stream()
               .map(LedgerDefinitionRequest.FlowDefinitionRequest::journalEntryType)
               .toList();

        validationContext.ifDuplicateValuesExist(journalEntryTypes, duplicates ->
              validationContext.error("Duplicate journal entry types detected: [%s]"
                    .formatted(String.join(",", duplicates))));
    }

    static void validateNoDuplicateLedgerLabelsExist(ValidationContext<LedgerDefinitionRequest> validationContext) {
        validationContext.ifDuplicateValuesExist(validationContext.getVal().labels, duplicates ->
              validationContext.error("Duplicate ledger labels detected: [%s]"
                    .formatted(String.join(",", duplicates))));
    }

    static void validateNoDuplicateLedgerAccountLabelsExist(ValidationContext<LedgerDefinitionRequest> validationContext) {
        final Map<String, List<String>> ledgerAccountLabels = validationContext.getVal()
              .ledgerAccounts
              .stream()
              .collect(groupingBy(LedgerAccountDefinitionRequest::alias,
                    flatMapping(ledgerAccountDefinitionRequest -> ledgerAccountDefinitionRequest.labels().stream(), toList())));

        ledgerAccountLabels.forEach((ledgerAlias, labels) ->

            validationContext.ifDuplicateValuesExist(labels, duplicates ->
                  validationContext.error("Ledger account: '%s' contains duplicate labels: [%s]"
                        .formatted(ledgerAlias, String.join(",", duplicates)))));
    }

    static void validateBeginningOfLedgerValue(ValidationContext<LedgerDefinitionRequest> validationContext) {
        final var beginningOfLedger = validationContext.getVal().beginningOfLedger;
        if (DateUtils.isValidInstant(beginningOfLedger).isEmpty()) {
            validationContext.error("Beginning of ledger value: '%s' is not valid.".formatted(beginningOfLedger));
        }
    }

    static void validateJournalEntryActions(ValidationContext<LedgerDefinitionRequest> validationContext) {

        final var ledgerDefinition = validationContext.getVal();
        final var allLedgerAccountAliases = ledgerDefinition.ledgerAccounts
              .stream()
              .map(LedgerAccountDefinitionRequest::alias)
              .collect(toSet());

        ledgerDefinition.flowsRequest.getAll().forEach(flowDefinitionRequest -> {
            flowDefinitionRequest.actions().forEach(action -> {
                    final var doubleEntryResult = JournalEntryActions.tryParseDoubleEntryAction(action);

                    if (doubleEntryResult.isPresent()) {
                        final var doubleEntry = doubleEntryResult.get();

                        validationContext.ifTrue(
                              Objects.equals(doubleEntry.firstAlias(), doubleEntry.secondAlias()),
                                errorInJournalEntryFlow(
                                      "Same ledger account alias value has been used. Action: '%s'".formatted(action),
                                      flowDefinitionRequest.flow(),
                                      flowDefinitionRequest.journalEntryType()));

                        validationContext.ifFalse(
                              allLedgerAccountAliases.contains(doubleEntry.firstAlias()),
                              errorInJournalEntryFlow(
                                    "Ledger account alias:'%s' has not been defined.".formatted(doubleEntry.firstAlias()),
                                    flowDefinitionRequest.flow(),
                                    flowDefinitionRequest.journalEntryType()));

                        validationContext.ifFalse(
                              allLedgerAccountAliases.contains(doubleEntry.secondAlias()),
                              errorInJournalEntryFlow(
                                    "Ledger account alias:'%s' has not been defined.".formatted(doubleEntry.secondAlias()),
                                    flowDefinitionRequest.flow(),
                                    flowDefinitionRequest.journalEntryType()));
                        return;
                    }

                    final var singleEntryResult = JournalEntryActions.tryParseSingleEntryAction(action);
                    if (singleEntryResult.isPresent()) {
                        final var singleEntry = singleEntryResult.get();

                        validationContext.ifFalse(
                              allLedgerAccountAliases.contains(singleEntry.alias()),
                              errorInJournalEntryFlow(
                                    "Ledger account alias:'%s' has not been defined.".formatted(singleEntry.alias()),
                                    flowDefinitionRequest.flow(),
                                    flowDefinitionRequest.journalEntryType()));

                        return;
                    }

                    validationContext.error(
                          errorInJournalEntryFlow("Could not parse: '%s'".formatted(action),
                                flowDefinitionRequest.flow(),
                                flowDefinitionRequest.journalEntryType()));

              });
        });
    }

    // t-accounts are always used in pairs, l-accounts always alone
    // Valid journal entry actions (structurally)
    // Missing currency in ledger account
    // Missing entities in ledger
    // Length: title, alias, labels,


    private static String errorInJournalEntryFlow(String message, String ledgerFlow, String journalEntryType) {
        return """
             %s
             Flow: %s, Journal entry type: %s
             """.formatted(message, ledgerFlow, journalEntryType);
    }

    private static String missingField(String fieldName) {
        return "Field: '%s' is missing".formatted(fieldName);
    }

}
