package org.example;

import static org.example.LedgerDefinitionParser.parse;
import static org.example.utils.ResourcesFileUtils.fileContents;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            final var parsedResult = parse(fileContents("src/main/resources/finance.ledger.json"));

            if (parsedResult.isLeft()) {
                System.out.println("--- Errors:");
                parsedResult.getLeft().forEach(System.out::println);
            } else {
                System.out.println("--- Valid");
            }
            Thread.sleep(2_000);
        }
    }
}