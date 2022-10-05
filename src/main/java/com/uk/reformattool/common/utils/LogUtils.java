package com.uk.reformattool.common.utils;

import com.uk.reformattool.common.model.BasicFileInfo;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.module.ModuleLevel;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogUtils {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public static void log(ModuleLevel level, String message) {
        Logger logger = LoggerFactory.getLogger(level.name());
        logger.info(message);
    }

    public static void log(ModuleLevel level, List<FileModel> fileModels) {
        Logger logger = LoggerFactory.getLogger(level.name());
        if (fileModels.isEmpty() && level.value > ModuleLevel.FILE_LEVEL.value) {
            logger.info("Couldn't find any file that matches conditions. All files are valid.");
            logger.info("Process is now closed.");
            return;
        }
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
        List<BasicFileInfo> datas = fileModels.stream().map(FileModel::toBasicFileInfo).collect(Collectors.toList());
        return CsvUtils.writeFile(tempFile, datas, BasicFileInfo.class);
    }
}
