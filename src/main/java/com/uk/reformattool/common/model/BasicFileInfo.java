package com.uk.reformattool.common.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Model for csv serialization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicFileInfo {

    @CsvBindByName(column = "uk_file_context")
    private String ukFileContext;
    @CsvBindByName(column = "relative_path")
    private String relativePath;
    @CsvBindByName(column = "file_name")
    private String fileName;

    public static BasicFileInfo create(String context, Path file) {
        String fullPath = file.getParent().toString();
        int index = fullPath.indexOf(context + "\\");
        return new BasicFileInfo(context, fullPath.substring(index + context.length() + 1), file.getFileName().toString());
    }
}
