package org.example;

import org.example.domain.FinLedger;
import org.example.domain.SingleInstanceFinLedger;
import org.example.repository.InMemoryGenericRepository;

import static org.example.parsing.LedgerDefinitionParser.parse;
import static org.example.utils.ResourcesFileUtils.fileContents;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        final FinLedger finLedger = new SingleInstanceFinLedger(
              InMemoryGenericRepository.INSTANCE,
              InMemoryGenericRepository.INSTANCE);



//        while (true) {
//            final var parsedResult = parse(fileContents("src/main/resources/operational_ledger.ledger.json"));
//
//            if (parsedResult.isLeft()) {
//                System.out.println("--- Errors:");
//                parsedResult.getLeft().forEach(System.out::println);
//            } else {
//                System.out.println("--- Valid");
//            }
//            Thread.sleep(2_000);
//        }
    }
}