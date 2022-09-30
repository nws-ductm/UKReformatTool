package com.uk.reformattool.filereader;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import com.uk.reformattool.common.utils.CsvUtils;
import com.uk.reformattool.scanner.model.FileModel;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ModuleService(flowTypes = FlowType.IMPORT_CSV, level = ModuleLevel.FILE_LEVEL)
public class CsvImportService extends AbstractModuleHandler {

    @Override
    @SneakyThrows(IOException.class)
    protected void execute(List<FileModel> fileModels) {
        Path csvDirectory = Paths.get(AppConfig.getInstance().getCsvDirectory());
        fileModels.addAll(CsvUtils.readFile(csvDirectory, FileModel.class));
    }
}
