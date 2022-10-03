package com.uk.reformattool.services.scanner;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.model.LineStatus;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.LogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

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
        return fileModels.stream().filter(FileModel::isValidForReformat).collect(Collectors.toList());
    }

    private void readFile(FileModel fileModel) {
        LineIterator iterator;
        try {
            iterator = FileUtils.lineIterator(fileModel.createFile());
        } catch (IOException e) {
            LogUtils.error(ModuleLevel.CONTENT_LEVEL, e);
            return;
        }

        int lineCount = 0;
        LineStatus status = new LineStatus();
        try {
            fileModel.setExist(true);
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                lineCount++;
                // Will end if reach class definition (ex: public class ...)
                if (line.contains("class " + fileModel.getClassName())) {
                    break;
                }
                // Check for whether current line is commented
                if (this.isCommented(line.trim(), status) || status.isCommented()) {
                    continue;
                }
                // Check for @Stateless position (annotation, import)
                if (line.contains(IMPORT_DEFINE)) {
                    fileModel.setImportStartPosition(lineCount);
                }
                if (line.contains(ANNOTATION_DEFINE)) {
                    fileModel.setAnnotationStartPosition(lineCount);
                }
                // Check for whether class has already defined @TransactionalAttribute at class level
                if (line.contains(TRAN_ATTR_DEFINE)) {
                    fileModel.setHasAnnotation(true);
                }
            }
        } finally {
            iterator.close();
        }
    }

    private boolean isCommented(String line, LineStatus status) {
        if (line.startsWith("//")) {
            status.setLineCommented(true);
            return true;
        }
        if (status.isBulkCommented()) {
            if (line.endsWith("*/")) {
                status.setBulkCommented(false);
            }
            return true;
        } else {
            if (line.startsWith("/*")) {
                status.setBulkCommented(true);
                return true;
            }
        }
        return false;
    }
}
