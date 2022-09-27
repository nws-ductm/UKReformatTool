package com.uk.reformattool.common.module;

import com.uk.reformattool.scanner.model.FileModel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base class for module service
 * Add in META-INF/services/com.uk.reformattool.common.module.AbstractModuleHandler for each implementation
 */
public abstract class AbstractModuleHandler {

    private AbstractModuleHandler nextHandler;

    protected abstract void execute(List<FileModel> fileModels);

    protected Logger getLogger() {
        return LogManager.getLogger(this.getClass());
    }

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
        Map<String, Long> resultCounts = fileModels.stream()
                .collect(Collectors.groupingBy(FileModel::getUkFileContext, Collectors.counting()));
        getLogger().info(resultCounts);
        if (nextHandler != null) {
            nextHandler.executeTask(level + 1, fileModels);
        }
    }

    public AbstractModuleHandler setNext(AbstractModuleHandler handler) {
        if (this.nextHandler == null) {
            this.nextHandler = handler;
        } else {
            this.nextHandler.setNext(handler);
        }
        return this;
    }

    private ModuleLevel getModuleLevel() {
        if (this.getClass().isAnnotationPresent(ModuleService.class)) {
            return this.getClass().getAnnotation(ModuleService.class).level();
        }
        throw new RuntimeException("Module service is not annotated!!");
    }
}
