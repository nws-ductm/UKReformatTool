package com.uk.reformattool.scanner.model;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public enum FileType {
    FINDER(1, Arrays.asList("Finder", "ScreenFinder")),
    QUERY(2, Arrays.asList("Query", "ScreenQuery")),
    QUERY_PROCESSOR(3, Arrays.asList("QueryProcessor", "QueryProcesser")),
    COMMAND_HANDLER(4, Arrays.asList("CommandHandler", "ScreenCommandHandler"));

    public int value;
    public List<String> suffixes;

    public static FileType getByName(String fileName) {
        return Stream.of(values()).filter(data -> data.suffixes.stream().anyMatch(fileName::contains))
                .findAny().orElse(null);
    }
}
