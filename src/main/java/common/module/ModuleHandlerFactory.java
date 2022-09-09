package common.module;

import java.util.Arrays;
import java.util.ServiceLoader;

public class ModuleHandlerFactory {

    public static AbstractModuleHandler get(FlowType flowType, ModuleLevel moduleLevel) {
        ServiceLoader<AbstractModuleHandler> serviceLoader = ServiceLoader.load(AbstractModuleHandler.class);
        for (AbstractModuleHandler instance : serviceLoader) {
            Class<? extends AbstractModuleHandler> c = instance.getClass();
            ModuleService annotation = c.getAnnotation(ModuleService.class);
            if (annotation != null && annotation.level().equals(moduleLevel)
                    && Arrays.asList(annotation.flowTypes()).contains(flowType)) {
                return instance;
            }
        }
        throw new RuntimeException("No suitable module implementation found!!");
    }
}
