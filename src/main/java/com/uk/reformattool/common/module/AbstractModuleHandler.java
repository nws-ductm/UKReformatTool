package com.uk.reformattool.common.module;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.scanner.model.FileModel;

import java.util.List;

/**
 * Base class for module service
 * Add in META-INF/services/com.uk.reformattool.common.module.AbstractModuleHandler for each implementation
 */
public abstract class AbstractModuleHandler {

    private AbstractModuleHandler nextHandler;

    protected abstract void execute(List<FileModel> fileModels);

    protected List<FileModel> postExecute(List<FileModel> fileModels) {
        return fileModels;
    }

    public void executeTask(int level, List<FileModel> fileModels) {
        int currentLevel = this.getModuleLevel().value;
        if (level == currentLevel || level == currentLevel - 1) {
            this.execute(fileModels);
            fileModels = this.postExecute(fileModels);
        } else {
            throw new RuntimeException("Wrong module execution order!!");
        }
        this.nextTask(level, fileModels);
    }

    public AbstractModuleHandler setNext(AbstractModuleHandler handler) {
        if (this.nextHandler == null) {
            this.nextHandler = handler;
        } else {
            this.nextHandler.setNext(handler);
        }
        return this;
    }

    private void nextTask(int level, List<FileModel> fileModels) {
        if (this.nextHandler != null) {
            this.nextHandler.executeTask(level, fileModels);
        }
    }

    private ModuleLevel getModuleLevel() {
        if (this.getClass().isAnnotationPresent(ModuleService.class)) {
            return this.getClass().getAnnotation(ModuleService.class).level();
        }
        throw new RuntimeException("Module service is not annotated!!");
    }
}
