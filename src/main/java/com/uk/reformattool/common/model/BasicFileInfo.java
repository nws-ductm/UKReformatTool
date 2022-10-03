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

    @CsvBindByName(column = "UK_File_Context")
    private String ukFileContext;
    @CsvBindByName(column = "Relative_Path")
    private String relativePath;
    @CsvBindByName(column = "File_Name")
    private String fileName;

    public static BasicFileInfo create(String context, Path file) {
        String fullPath = file.getParent().toString();
        int index = fullPath.indexOf(context + "\\");
        return new BasicFileInfo(context, fullPath.substring(index + context.length() + 1), file.getFileName().toString());
    }
}
