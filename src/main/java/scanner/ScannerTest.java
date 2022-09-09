package scanner;

import common.module.AbstractModuleHandler;
import common.module.FlowType;
import common.module.ModuleHandlerFactory;
import common.module.ModuleLevel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import scanner.model.FileModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScannerTest {
    final static Logger logger = LogManager.getLogger(ScannerTest.class);

    public static void main(String[] args) {

        AbstractModuleHandler service = ModuleHandlerFactory.get(FlowType.SCAN_WORKSPACE, ModuleLevel.FILE_LEVEL);
        List<FileModel> fileModels = Collections.synchronizedList(new LinkedList<>());
        service.executeTask(ModuleLevel.FILE_LEVEL.value, fileModels);
        Map<String, Long> resultCounts = fileModels.stream()
                .collect(Collectors.groupingBy(FileModel::getUkFileContext, Collectors.counting()));
        logger.info(resultCounts);
    }
}
