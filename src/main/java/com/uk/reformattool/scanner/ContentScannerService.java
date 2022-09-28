package com.uk.reformattool.scanner;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.scanner.model.FileModel;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ModuleService(level = ModuleLevel.CONTENT_LEVEL)
public class ContentScannerService extends AbstractModuleHandler {

    private static final String IMPORT_DEFINE = "import javax.ejb.Stateless;";
    private static final String ANNOTATION_DEFINE = "@Stateless";
    private static final String TRAN_ATTR_DEFINE = "@TransactionAttribute";

    @Override
    protected void execute(List<FileModel> fileModels) {
        // Read each file from fileModels and update index
        fileModels.parallelStream().forEach(this::readFile);
    }

    @Override
    protected List<FileModel> postExecute(List<FileModel> fileModels) {
        // TODO: Log list to file csv
        List<FileModel> results = fileModels.stream().filter(FileModel::isValidForReformat).collect(Collectors.toList());
//        getLogger().info(results);
        return results;
    }

    @SneakyThrows({FileNotFoundException.class, IOException.class})
    private void readFile(FileModel fileModel) {
        LineIterator iterator = FileUtils.lineIterator(fileModel.createFile());
        int lineCount = 0;
        try {
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                lineCount++;
                if (line.contains("class " + fileModel.getClassName())) {
                    break;
                }
                if (line.contains(IMPORT_DEFINE)) {
                    fileModel.setImportStartPosition(lineCount);
                }
                if (line.contains(ANNOTATION_DEFINE)) {
                    fileModel.setAnnotationStartPosition(lineCount);
                }
                if (line.contains(TRAN_ATTR_DEFINE)) {
                    fileModel.setHasAnnotation(true);
                }
            }
        } finally {
            iterator.close();
        }
    }
}
