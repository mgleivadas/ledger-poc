package org.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;

public final class LedgerDefinitionRequest {

    @JsonCreator
    public LedgerDefinitionRequest(
          @JsonProperty("alias") String alias,
          @JsonProperty("title") String title,
          @JsonProperty("beginning_of_ledger_utc") String beginningOfLedgerUtc,
          @JsonProperty("labels") List<String> labels,
          @JsonProperty("ledger_accounts") List<LedgerAccountDefinitionRequest> ledgerAccounts,
          @JsonProperty("flows") LinkedHashMap<String, LinkedHashMap<String, List<String>>> flows) {

        this.alias = alias;
        this.title = title;
        this.beginningOfLedger = beginningOfLedgerUtc;
        this.labels = labels;
        this.ledgerAccounts = ledgerAccounts;
        this.flowsRequest = new FlowsRequest(flows);
    }

    public final String alias;
    public final String title;
    public final String beginningOfLedger;
    public final List<String> labels;
    public final List<LedgerAccountDefinitionRequest> ledgerAccounts;
    public final FlowsRequest flowsRequest;

    public record LedgerAccountDefinitionRequest(
          String alias,
          String title,
          String category,
          String type,
          String signage,
          String currency,
          @JsonProperty("initial_value") String initialValue,
          List<String> labels) {}


    public static final class FlowsRequest {

        public FlowsRequest(LinkedHashMap<String, LinkedHashMap<String, List<String>>> flows) {
            this.flows = flows;
        }

        List<FlowDefinitionRequest> getAll() {
            return flows.entrySet()
                  .stream()
                  .flatMap(flowEntry ->
                        flowEntry.getValue().entrySet().stream().map(e ->
                              new FlowDefinitionRequest(flowEntry.getKey(), e.getKey(), e.getValue())))
                  .toList();
        }

        private final LinkedHashMap<String, LinkedHashMap<String, List<String>>> flows;
    }

    public record FlowDefinitionRequest(String flow, String journalEntryType, List<String> actions) {}
}
