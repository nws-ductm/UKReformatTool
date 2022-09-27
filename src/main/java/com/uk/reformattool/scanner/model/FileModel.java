package com.uk.reformattool.scanner.model;

import com.uk.reformattool.common.utils.AppConfig;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;

@Data
public class FileModel implements Comparable<FileModel> {
    private final String ukFileContext;
    private final String relativePath;
    private final String fileName;
    private final String className;
    private final FileType fileType;
    private Integer importStartPosition;
    private Integer annotationStartPosition;
    private boolean hasAnnotation;

    public FileModel(String context, Path file) {
        String fullPath = file.getParent().toString();
        int index = fullPath.indexOf(context + "\\");
        this.ukFileContext = context;
        this.relativePath = fullPath.substring(index + context.length() + 1);
        this.fileName = file.getFileName().toString();
        this.fileType = FileType.getByName(fileName);
        this.className = fileName.replace(".java", "");
    }

    public File createFile() {
        String filePath = AppConfig.rootDirectory() + "/%s/%s/%s";
        return new File(String.format(filePath, ukFileContext, relativePath, fileName));
    }

    public boolean isValidForReformat() {
        return this.importStartPosition != null && this.annotationStartPosition != null && !this.hasAnnotation;
    }

    public String toString() {
        return this.ukFileContext + ": " + this.className;
    }

    @Override
    public int compareTo(FileModel o) {
        return Comparator.comparing(FileModel::getUkFileContext)
                .thenComparing(FileModel::getRelativePath)
                .thenComparing(FileModel::getFileType)
                .thenComparing(FileModel::getClassName).compare(this, o);
    }
}
