package com.uk.reformattool.common.model;

import com.uk.reformattool.common.utils.AppConfig;
import lombok.Data;

import java.io.File;
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
    private boolean isExist;

    public FileModel(BasicFileInfo basicFileInfo) {
        this.ukFileContext = basicFileInfo.getUkFileContext();
        this.relativePath = basicFileInfo.getRelativePath();
        this.fileName = basicFileInfo.getFileName();
        this.fileType = FileType.getByName(fileName);
        this.className = fileName.replace(".java", "");
    }

    public File createFile() {
        String filePath = AppConfig.getInstance().getRootDirectory() + "/%s/%s/%s";
        return new File(String.format(filePath, ukFileContext, relativePath, fileName));
    }

    public BasicFileInfo toBasicFileInfo() {
        return new BasicFileInfo(ukFileContext, relativePath, fileName);
    }

    public boolean isValidForReformat() {
        return this.isExist && this.importStartPosition != null && this.annotationStartPosition != null
                && !this.hasAnnotation;
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
