package scanner.model;

import lombok.Data;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@Data
public class FileModel {
    private final String ukFileContext;
    private final String relativePath;
    private final String fileName;
    private final FileType fileType;
    private int importStartPosition;
    private int annotationStartPosition;

    public FileModel(String context, Path file) {
        String fullPath = file.getParent().toString();
        int index = fullPath.indexOf(context + "\\");
        this.ukFileContext = context;
        this.relativePath = fullPath.substring(index + context.length() + 1);
        this.fileName = file.getFileName().toString();
        this.fileType = FileType.getByName(fileName);
    }

    public RandomAccessFile createFile() throws FileNotFoundException {
        String filePath = "/%s/%s/%s.java";
        return new RandomAccessFile(String.format(filePath, ukFileContext, relativePath, fileName), "rw");
    }
}
