package com.uk.reformattool.common.utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvBindByName;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.scanner.model.FileModel;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogUtils {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public static void log(ModuleLevel level, String message) {
        Logger logger = LoggerFactory.getLogger(level.name());
        logger.info(message);
    }

    public static void log(ModuleLevel level, List<FileModel> fileModels) {
        Logger logger = LoggerFactory.getLogger(level.name());
        Map<String, Long> resultCounts = fileModels.stream()
                .collect(Collectors.groupingBy(FileModel::getUkFileContext, Collectors.counting()));
        logger.info("Currently processing: {}", resultCounts);
        logger.info("Total files: " + fileModels.size());

        if (level.value == ModuleLevel.CONTENT_LEVEL.value) {
            String path = writeCsvFile(fileModels);
            logger.info("Detail of processed files is located at: " + path);
        }
    }

    public static void error(ModuleLevel level, Throwable e) {
        Logger logger = LoggerFactory.getLogger(level.name());
        logger.error(e.getMessage());
    }

    @SneakyThrows
    private static String writeCsvFile(List<FileModel> fileModels) {
        Path path = Paths.get(TEMP_DIR).resolve(AppConfig.getInstance().getTempDirectory());
        if (!path.toFile().exists()) {
            Files.createDirectory(path);
        }
        Path tempFile = Files.createTempFile(path, AppConfig.getInstance().getTempPrefix(), ".csv");

        try (Writer in = new FileWriter(tempFile.toString())) {
            CSVWriter writer = new CSVWriter(in, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
            List<Field> fields = Arrays.asList(FileModel.class.getDeclaredFields());
            writer.writeNext(fields.stream().filter(f -> f.isAnnotationPresent(CsvBindByName.class))
                    .map(Field::getName).toArray(String[]::new));
            for (FileModel fileModel : fileModels) {
                writer.writeNext(fields.stream().map(f -> {
                    if (f.isAnnotationPresent(CsvBindByName.class)) {
                        try {
                            f.setAccessible(true);
                            return String.valueOf(f.get(fileModel));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).toArray(String[]::new));
            }
            writer.close();
        }
        return tempFile.toString();
    }
}
