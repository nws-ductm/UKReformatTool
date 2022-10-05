package com.uk.reformattool.services.scanner;

import com.uk.reformattool.common.AppConst;
import com.uk.reformattool.common.annotations.ModuleService;
import com.uk.reformattool.common.model.BasicApiInfo;
import com.uk.reformattool.common.model.FileModel;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import com.uk.reformattool.common.utils.AppConfig;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@ModuleService(flowTypes = FlowType.IMPORT_CSV, level = ModuleLevel.FILE_LEVEL)
public class ApiScannerService extends AbstractModuleHandler {

    private static final String WEB_DIRECTORY_PATTERN = "(.+\\.web)|ClientApp.+$";
    private static final String API_PATTERN = "(/?[\\w_-]+/){3,}[\\w_-]*";
    private static final String FILE_PATTERN = "(?:(%s).?(%s)?.?(%s)?)$";

    @Override
    @SuppressWarnings("unchecked")
    protected void execute(List<FileModel> fileModels) {
        List<BasicApiInfo> apiInfos = (List<BasicApiInfo>) this.paramMap.get(AppConst.KEY_API_INFO);
        List<String> contexts = (List<String>) this.paramMap.get(AppConst.KEY_CONTEXT);
        Path rootDirectory = Paths.get(AppConfig.getInstance().getRootDirectory());
        List<String> results = Collections.synchronizedList(new LinkedList<>());
        contexts.parallelStream().forEach(context -> results.addAll(this.scanForApis(rootDirectory.resolve(context), apiInfos)));
        this.paramMap.put(AppConst.KEY_API_LIST, results.stream().distinct().collect(Collectors.toList()));
    }

    @Override
    public int subModuleLevel() {
        return AppConst.SUB_LEVEL_2;
    }

    @Override
    public boolean canExecute() {
        return AppConfig.getInstance().isImportAsAPI();
    }

    @SneakyThrows
    private List<String> scanForApis(Path rootDirectory, List<BasicApiInfo> apiInfos) {
        List<String> results = Collections.synchronizedList(new LinkedList<>());
        String[] prefixes = apiInfos.stream().map(BasicApiInfo::getProgramIdPrefix).distinct().toArray(String[]::new);
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile() && file.getFileName().toString().endsWith(".ts")
                        && StringUtils.containsAny(file.getFileName().toString(), prefixes)) {
                    List<String> lines = FileUtils.readLines(file.toFile(), Charset.defaultCharset());
                    lines.forEach(line -> {
                        Matcher matcher = findPattern(API_PATTERN, line);
                        if (matcher.find()) {
                            results.add(matcher.group());
                        }
                    });
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String currentDir = dir.toString().replace(rootDirectory.toString(), "");
                if (currentDir.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                if (findPattern(WEB_DIRECTORY_PATTERN, currentDir).find()) {
                    if (currentDir.contains("view\\")) {
                        List<String> patterns = apiInfos.stream()
                                .map(data -> String.format(FILE_PATTERN, data.getProgramIdPrefix(), data.getProgramIdSuffix(),
                                        data.getScreenId()))
                                .collect(Collectors.toList());
                        // Filter for selected url
                        if (!findPattern(String.join("|", patterns), currentDir).find()) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return results;
    }
}
