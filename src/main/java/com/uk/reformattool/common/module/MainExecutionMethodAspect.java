package com.uk.reformattool.common.module;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.utils.LogUtils;
import com.uk.reformattool.common.utils.TimerUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.List;

@Aspect
public class MainExecutionMethodAspect {

    /**
     * Pointcut for execution executeTask()
     * Responsibility: Log general messages, start timer
     */
    @Pointcut("execution(* AbstractModuleHandler+.executeTask(..))")
    private void executeTask() {
    }

    /**
     * Pointcut for call nextTask()
     * Responsibility: end timer
     */
    @Pointcut("withincode(* AbstractModuleHandler+.executeTask(..)) && call(* AbstractModuleHandler+.nextTask(..))")
    private void nextTask() {
    }

    @Around("executeTask()")
    public Object aroundExecuteTask(ProceedingJoinPoint joinPoint) {
        TimerUtils.start();
        // Get module level
        Class<?> c = joinPoint.getTarget().getClass();
        ModuleService annotation = c.getAnnotation(ModuleService.class);
        ModuleLevel level = annotation.level();
        // Process start
        LogUtils.log(level, "Process start...");
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // Handle exception
            e.printStackTrace();
            LogUtils.error(level, e);
            return null;
        }
    }

    @Before("nextTask()")
    @SuppressWarnings("unchecked")
    public void beforeNextTask(JoinPoint joinPoint) {
        // Get module level
        Class<?> c = joinPoint.getTarget().getClass();
        ModuleService annotation = c.getAnnotation(ModuleService.class);
        ModuleLevel level = annotation.level();
        // Process end
        LogUtils.log(level, String.format("Process run time: %.3fs", TimerUtils.end()));
        // Log results
        LogUtils.log(level, (List<FileModel>) joinPoint.getArgs()[1]);
    }
}
