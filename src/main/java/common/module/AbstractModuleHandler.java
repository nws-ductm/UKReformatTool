package common.module;

import scanner.model.FileModel;

import java.util.List;

/**
 * Base class for module service
 * Add in META-INF/services/common.module.AbstractModuleHandler for each implementation
 */
public abstract class AbstractModuleHandler {
    private AbstractModuleHandler nextHandler;

    protected abstract void execute(List<FileModel> fileModels);

    public void executeTask(int level, List<FileModel> fileModels) {
        int currentLevel = this.getModuleLevel().value;
        if (level == currentLevel) {
            this.execute(fileModels);
        } else {
            throw new RuntimeException("Wrong module execution order!!");
        }
        if (nextHandler != null) {
            nextHandler.executeTask(level + 1, fileModels);
        }
    }

    public void setNext(AbstractModuleHandler handler) {
        this.nextHandler = handler;
    }

    private ModuleLevel getModuleLevel() {
        if (this.getClass().isAnnotationPresent(ModuleService.class)) {
            return this.getClass().getAnnotation(ModuleService.class).level();
        }
        throw new RuntimeException("Module service is not annotated!!");
    }
}
