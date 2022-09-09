package scanner;

import common.module.AbstractModuleHandler;
import common.module.ModuleLevel;
import common.module.ModuleService;
import scanner.model.FileModel;

import java.util.List;

@ModuleService(level = ModuleLevel.CONTENT_LEVEL)
public class ContentScannerService extends AbstractModuleHandler {

    @Override
    protected void execute(List<FileModel> fileModels) {

    }

    private void readFile(FileModel fileModel) {

    }
}
