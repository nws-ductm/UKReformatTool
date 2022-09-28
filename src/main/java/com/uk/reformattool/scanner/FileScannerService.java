package com.uk.reformattool.scanner;

import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import com.uk.reformattool.scanner.model.FileModel;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ModuleService(flowTypes = FlowType.SCAN_WORKSPACE, level = ModuleLevel.FILE_LEVEL)
public class FileScannerService extends AbstractModuleHandler {

    private static final String FILE_PATTERN = "(%s)\\w*.java";
    private static final String DIRECTORY_PATTERN = "nts\\.uk\\..+app(\\\\src\\\\main\\\\java.*|\\\\src\\\\main|\\\\src)*$" +
            "|^\\\\[^.]((?!nts\\.uk\\..+).)*$";

    @Override
    protected void execute(List<FileModel> fileModels) {
        Path rootDirectory = Paths.get(AppConfig.getInstance().getRootDirectory());
        List<String> contexts = this.getAllContexts(rootDirectory);
        contexts.parallelStream().forEach(context -> this.scanFiles(context, rootDirectory.resolve(context), fileModels));
    }

    @Override
    protected List<FileModel> postExecute(List<FileModel> fileModels) {
        return fileModels.stream().sorted(Comparator.comparing(Function.identity()))
                .collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    private void scanFiles(String context, Path rootDirectory, final List<FileModel> files) {
        final String filePattern = String.format(FILE_PATTERN, String.join("|", AppConfig.getInstance().getSuffixes()));
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    String fileName = file.getFileName().toString();
                    Pattern pattern = Pattern.compile(filePattern);
                    Matcher matcher = pattern.matcher(fileName);
                    if (matcher.find()) {
                        files.add(new FileModel(context, file));
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String currentDir = dir.toString().replace(rootDirectory.toString(), "");
                Pattern pattern = Pattern.compile(DIRECTORY_PATTERN);
                if (currentDir.isEmpty() || pattern.matcher(currentDir).find()) {
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
    }

    @SneakyThrows(IOException.class)
    private List<String> getAllContexts(Path rootDirectory) {
        List<String> results = new ArrayList<>();
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.toString().equals(rootDirectory.toString())) {
                    results.add(dir.getFileName().toString());
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return results;
    }
}
