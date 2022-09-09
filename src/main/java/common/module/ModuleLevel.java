package common.module;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ModuleLevel {
    FILE_LEVEL(1),
    CONTENT_LEVEL(2),
    FORMAT_LEVEL(3);

    public int value;
}
