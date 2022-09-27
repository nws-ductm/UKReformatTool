package com.uk.reformattool.scanner;

import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleHandlerFactory;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import com.uk.reformattool.scanner.model.FileModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScannerTest {

    public static void main(String[] args) {

        AppConfig.init();

        AbstractModuleHandler service = ModuleHandlerFactory.get(FlowType.SCAN_WORKSPACE, ModuleLevel.FILE_LEVEL);
        service.setNext(ModuleHandlerFactory.get(FlowType.SCAN_WORKSPACE, ModuleLevel.CONTENT_LEVEL))
                .setNext(ModuleHandlerFactory.get(FlowType.SCAN_WORKSPACE, ModuleLevel.FORMAT_LEVEL));
        List<FileModel> fileModels = Collections.synchronizedList(new LinkedList<>());
        service.executeTask(ModuleLevel.FILE_LEVEL.value, fileModels);
    }
}
