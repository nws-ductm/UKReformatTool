package com.uk.reformattool.common.module;

import com.uk.reformattool.common.annotations.ModuleService;

import java.util.*;

public class ModuleHandlerFactory {

    public static AbstractModuleHandler get(FlowType flowType, ModuleLevel moduleLevel) {
        ServiceLoader<AbstractModuleHandler> serviceLoader = ServiceLoader.load(AbstractModuleHandler.class);
        List<AbstractModuleHandler> validModules = new ArrayList<>();
        for (AbstractModuleHandler instance : serviceLoader) {
            Class<? extends AbstractModuleHandler> c = instance.getClass();
            ModuleService annotation = c.getAnnotation(ModuleService.class);
            if (annotation != null && annotation.level().equals(moduleLevel)
                    && Arrays.asList(annotation.flowTypes()).contains(flowType) && instance.canExecute()) {
                if (instance.subModuleLevel() >= 0) {
                    validModules.add(instance);
                } else {
                    return instance;
                }
            }
        }
        if (!validModules.isEmpty()) {
            validModules.sort(Comparator.comparing(AbstractModuleHandler::subModuleLevel));
            AbstractModuleHandler result = validModules.get(0);
            validModules.stream().skip(1).forEach(result::setNext);
            return result;
        }
        throw new RuntimeException("No suitable module implementation found!!");
    }
}
