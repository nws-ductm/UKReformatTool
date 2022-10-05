package com.uk.reformattool.common.module;

import com.uk.reformattool.common.AppConst;
import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.FileModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for module service
 * Add in META-INF/services/com.uk.reformattool.common.module.AbstractModuleHandler for each implementation
 */
public abstract class AbstractModuleHandler {

    protected Map<String, Object> paramMap = new HashMap<>();
    private AbstractModuleHandler nextHandler;

    /**
     * Execute module specific task
     *
     * @param fileModels
     */
    protected abstract void execute(List<FileModel> fileModels);

    /**
     * Additional execution after execute() is called
     *
     * @param fileModels
     * @return
     */
    protected List<FileModel> postExecute(List<FileModel> fileModels) {
        return fileModels;
    }

    /**
     * Additional module execution condition, except from those that are defined in @ModuleService
     *
     * @return
     */
    public boolean canExecute() {
        return true;
    }

    /**
     * Additional sub module level (-1 is no sub module)
     *
     * @return
     */
    public int subModuleLevel() {
        return AppConst.NO_SUBMODULE;
    }

    public void executeTask(int level, List<FileModel> fileModels) {
        int currentLevel = this.getModuleLevel().value;
        if (level == currentLevel || level == currentLevel - 1) {
            this.execute(fileModels);
            fileModels = this.postExecute(fileModels);
        } else {
            throw new RuntimeException("Wrong module execution order!!");
        }
        this.nextTask(currentLevel, fileModels);
    }

    public AbstractModuleHandler setNext(AbstractModuleHandler handler) {
        handler.paramMap = this.paramMap;
        if (this.nextHandler == null) {
            this.nextHandler = handler;
        } else {
            this.nextHandler.setNext(handler);
        }
        return this;
    }

    private void nextTask(int level, List<FileModel> fileModels) {
        if (this.nextHandler != null && (!fileModels.isEmpty() || this.nextHandler.getModuleLevel().value < ModuleLevel.CONTENT_LEVEL.value)) {
            this.nextHandler.executeTask(level, fileModels);
        }
    }

    private ModuleLevel getModuleLevel() {
        if (this.getClass().isAnnotationPresent(ModuleService.class)) {
            return this.getClass().getAnnotation(ModuleService.class).level();
        }
        throw new RuntimeException("Module service is not annotated!!");
    }

    protected Matcher findPattern(String regex, String s) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(s);
    }
}
