package com.uk.reformattool.common.annotations;

import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleService {
    ModuleLevel level();

    FlowType[] flowTypes() default {FlowType.SCAN_WORKSPACE, FlowType.IMPORT_CSV};
}
