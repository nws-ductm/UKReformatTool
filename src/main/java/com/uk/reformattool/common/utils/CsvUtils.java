package com.uk.reformattool.common.utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CsvUtils {

    public static <T> String writeFile(Path destination, List<T> datas, Class<T> c) throws IOException {
        try (Writer in = new FileWriter(destination.toString())) {
            CSVWriter writer = new CSVWriter(in, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            List<Field> fields = Arrays.asList(c.getDeclaredFields());
            writer.writeNext(fields.stream().filter(f -> f.isAnnotationPresent(CsvBindByName.class))
                    .map(f -> f.getAnnotation(CsvBindByName.class).column()).toArray(String[]::new));
            for (Object data : datas) {
                writer.writeNext(fields.stream().map(f -> {
                    if (f.isAnnotationPresent(CsvBindByName.class)) {
                        try {
                            f.setAccessible(true);
                            return String.valueOf(f.get(data));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).toArray(String[]::new));
            }
            writer.close();
        }
        return destination.toString();
    }

    public static <T> List<T> readFile(Path file, Class<T> c) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            CsvToBean<T> cb = new CsvToBeanBuilder<T>(reader)
                    .withType(c).build();
            return cb.parse();
        }
    }
}
