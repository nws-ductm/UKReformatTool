package scanner;

import common.module.AbstractModuleHandler;
import common.module.FlowType;
import common.module.ModuleLevel;
import common.module.ModuleService;
import lombok.SneakyThrows;
import scanner.model.FileModel;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleService(flowTypes = FlowType.SCAN_WORKSPACE, level = ModuleLevel.FILE_LEVEL)
public class FileScannerService extends AbstractModuleHandler {

    private static final String FILE_PATTERN = "(Query|QueryProcessor|QueryProcesser|Finder)\\w*.java";
    private static final String DIRECTORY_PATTERN = "nts\\.uk\\..+app(\\\\src\\\\main\\\\java.*|\\\\src\\\\main|\\\\src)*$" +
            "|^\\\\[^.]((?!nts\\.uk\\..+).)*$";

    @Override
    protected void execute(List<FileModel> fileModels) {
        // TODO: switch to relative directory
        Path rootDirectory = Paths.get("D:/Workspace/UK_test/UniversalK/nts.uk");
        List<String> contexts = this.getAllContexts(rootDirectory);
        contexts.parallelStream().forEach(context -> this.scanFiles(context, rootDirectory.resolve(context), fileModels));
    }

    @SneakyThrows(IOException.class)
    private void scanFiles(String context, Path rootDirectory, final List<FileModel> files) {
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    String fileName = file.getFileName().toString();
                    Pattern pattern = Pattern.compile(FILE_PATTERN);
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
