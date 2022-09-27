package com.uk.reformattool.common.module;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FlowType {
    SCAN_WORKSPACE(1), // FinderクラスとqueryProcessorクラス
    IMPORT_CSV(2);     // commandHandler

    public int value;
}
