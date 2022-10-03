package com.uk.reformattool.main;

import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleHandlerFactory;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;

import java.util.Collections;
import java.util.LinkedList;

public class MainApplication {
    public static void main(String[] args) {
        // Init app config
        AppConfig.init();

        // Init flow
        FlowType flowType = AppConfig.getInstance().getMode();
        AbstractModuleHandler service = buildFlow(flowType);
        service.executeTask(ModuleLevel.FILE_LEVEL.value, Collections.synchronizedList(new LinkedList<>()));
    }

    private static AbstractModuleHandler buildFlow(FlowType flowType) {
        AbstractModuleHandler service = ModuleHandlerFactory.get(flowType, ModuleLevel.FILE_LEVEL);
        if (AppConfig.getInstance().isScanContent()) {
            service = service.setNext(ModuleHandlerFactory.get(flowType, ModuleLevel.CONTENT_LEVEL));
            if (AppConfig.getInstance().isReformatFile()) {
                service = service.setNext(ModuleHandlerFactory.get(flowType, ModuleLevel.FORMAT_LEVEL));
            }
        }
        return service;
    }
}
