package com.uk.reformattool.services.scanner;

import com.uk.reformattool.common.AppConst;
import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.BasicFileInfo;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@ModuleService(flowTypes = FlowType.IMPORT_CSV, level = ModuleLevel.FILE_LEVEL)
public class WebServiceScannerService extends AbstractModuleHandler {

    private static final String WS_DIRECTORY_PATTERN = "nts\\.uk\\..+ws(\\\\src\\\\main\\\\java.*|\\\\src\\\\main|\\\\src)*$" +
            "|^\\\\[^.]((?!nts\\.uk\\..+).)*$";
    private static final String PATH_ANNO_PATTERN = "@Path\\(\\\"(.+)\\\"\\)";
    private static final String DI_PATTERN = "(\\w+)\\s(\\w+);$";
    private static final String DI_CALL_PATTERN = "(?:this.)?(\\w+)\\.handle\\(.+(?:\\);)?$";

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(List<FileModel> fileModels) {
        List<String> contexts = (List<String>) this.paramMap.get(AppConst.KEY_CONTEXT);
        Path rootDirectory = Paths.get(AppConfig.getInstance().getRootDirectory());
        List<String> apiList = (List<String>) this.paramMap.get(AppConst.KEY_API_LIST);
        List<FileModel> results = contexts.parallelStream().map(context -> {
            Set<String> filesNames = this.getFileNames(rootDirectory.resolve(context), apiList);
            List<Path> files = this.getFileInfos(rootDirectory.resolve(context), filesNames);
            return files.stream().map(file -> new FileModel(BasicFileInfo.create(context, file)))
                    .collect(Collectors.toList());
        }).flatMap(List::stream).collect(Collectors.toList());
        fileModels.addAll(results);
    }

    @Override
    public int subModuleLevel() {
        return AppConst.SUB_LEVEL_3;
    }

    @Override
    public boolean canExecute() {
        return AppConfig.getInstance().isImportAsAPI();
    }

    @SneakyThrows
    private Set<String> getFileNames(Path rootDirectory, List<String> apiList) {
        Set<String> results = new HashSet<>();
        Map<String, String> definedInjections = new HashMap<>();
        AtomicBoolean foundInjected = new AtomicBoolean(false);
        AtomicBoolean foundApiCall = new AtomicBoolean(false);

        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile()) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".java")) {
                        String rootWsPath = null;
                        for (LineIterator it = FileUtils.lineIterator(file.toFile()); it.hasNext(); ) {
                            String line = it.next();
                            // Check for @Inject
                            // If has @Inject, look for next CommandHandler definition
                            if (line.trim().startsWith("@Inject")) {
                                foundInjected.set(true);
                            }
                            if (foundInjected.get()) {
                                Matcher matcher = findPattern(DI_PATTERN, line);
                                if (matcher.find()) {
                                    definedInjections.put(matcher.group(2), matcher.group(1) + ".java");
                                    foundInjected.set(false);
                                }
                            }
                            // Check for @Path(...)
                            if (line.trim().startsWith("@Path")) {
                                Matcher matcher = findPattern(PATH_ANNO_PATTERN, line);
                                if (rootWsPath == null) {
                                    if (matcher.find()) {
                                        rootWsPath = matcher.group(1);
                                    }
                                } else if (matcher.find()) {
                                    String api = String.join("/", Arrays.asList(rootWsPath, matcher.group(1)));
                                    if (apiList.contains(api)) {
                                        foundApiCall.set(true);
                                    }
                                    apiList.remove(api);
                                }
                            }
                            if (foundApiCall.get()) {
                                Matcher matcher = findPattern(DI_CALL_PATTERN, line);
                                if (matcher.find()) {
                                    results.add(definedInjections.get(matcher.group(1)));
                                    foundApiCall.set(false);
                                }
                            }
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String currentDir = dir.toString().replace(rootDirectory.toString(), "");
                if (currentDir.isEmpty() || findPattern(WS_DIRECTORY_PATTERN, currentDir).find()) {
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return results;
    }

    @SneakyThrows
    private List<Path> getFileInfos(Path rootDirectory, Set<String> names) {
        List<Path> results = new ArrayList<>();
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (names.contains(file.getFileName().toString())) {
                    results.add(file);
                    names.remove(file.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return results;
    }
}
