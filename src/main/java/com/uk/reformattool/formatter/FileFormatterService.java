package com.uk.reformattool.formatter;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.scanner.model.FileModel;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ModuleService(level = ModuleLevel.FORMAT_LEVEL)
public class FileFormatterService extends AbstractModuleHandler {

    @Override
    protected void execute(List<FileModel> fileModels) {
        fileModels.parallelStream().forEach(this::formatFile);
    }

    @SneakyThrows
    private void formatFile(FileModel fileModel) {
        Path path = Paths.get(fileModel.createFile().toURI());
        List<String> lines = Files.readAllLines(path);
        // Insert annotation
        lines.add(fileModel.getAnnotationStartPosition(), "@TransactionAttribute(TransactionAttributeType.SUPPORTS)");
        // Insert import
        lines.add(fileModel.getImportStartPosition(), "import javax.ejb.TransactionAttribute;\nimport javax.ejb.TransactionAttributeType;");
        Files.write(path, lines);
    }
}
