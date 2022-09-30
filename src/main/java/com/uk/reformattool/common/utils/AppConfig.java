package com.uk.reformattool.common.utils;

import com.uk.reformattool.common.annotations.PropValue;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import com.uk.reformattool.common.module.ModuleLevel;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;

    @PropValue("application.target.directory")
    private String rootDirectory;

    @PropValue("application.csv.directory")
    private String csvDirectory;

    @PropValue("application.temp.directory")
    private String tempDirectory;

    @PropValue("application.temp.prefix")
    private String tempPrefix;

    @PropValue("application.settings.mode")
    private int mode;

    @PropValue("application.settings.scan.suffixes")
    private List<String> suffixes;

    @PropValue("application.settings.scan.supportedContexts")
    private List<String> contexts;

    @PropValue("application.settings.scanContent")
    private boolean scanContent;

    @PropValue("application.settings.reformatFile")
    private boolean reformatFile;

    public static boolean init() {
        try {
            Properties properties = properties();
            if (properties == null) {
                return false;
            }
            instance = new AppConfig();
            for (Field field : AppConfig.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(PropValue.class)) {
                    String value = properties.getProperty(field.getAnnotation(PropValue.class).value(), "");
                    Class<?> c = field.getType();
                    if (int.class.isAssignableFrom(c) || Integer.class.isAssignableFrom(c)) {
                        field.setInt(instance, Integer.parseInt(value));
                    } else if (boolean.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c)) {
                        field.setBoolean(instance, Boolean.parseBoolean(value));
                    } else if (List.class.isAssignableFrom(c)) {
                        field.set(instance, Arrays.asList(value.split(",\\s*")));
                    } else {
                        field.set(instance, value);
                    }
                }
            }
            instance.scanContexts(instance.rootDirectory);
            return true;
        } catch (IOException | IllegalAccessException e) {
            LogUtils.error(ModuleLevel.SETTING_LEVEL, e);
            return false;
        } finally {
            AppConfig.initMetaFile(AbstractModuleHandler.class);
        }
    }

    public static AppConfig getInstance() {
        return instance;
    }

    public static Properties properties() {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            LogUtils.log(ModuleLevel.SETTING_LEVEL, "Error loading application.properties: File not found");
            return null;
        }
    }

    @SneakyThrows
    private static <T> void initMetaFile(Class<T> c) {
        // Get resource file
        String fullFileName = c.getTypeName();

        URL resource = c.getResource("/META-INF/services/" + fullFileName);
        if (resource != null) {
            File file = new File(resource.toURI());
            // Search for implementing classes
            Reflections reflections = new Reflections("com.uk.reformattool");
            Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(c);
            // Write into resource file
            List<String> lines = subTypes.stream().map(Class::getTypeName).collect(Collectors.toList());
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        }
    }

    public FlowType getMode() {
        return Stream.of(FlowType.values()).filter(data -> data.value == instance.mode).findFirst().orElse(null);
    }

    public void saveConfig() {
        try {
            Properties properties = properties();

            for (Field field : AppConfig.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(PropValue.class)) {
                    String key = field.getAnnotation(PropValue.class).value();
                    String value = String.valueOf(field.get(this));
                    Objects.requireNonNull(properties).setProperty(key, value);
                }
            }
            LogUtils.log(ModuleLevel.SETTING_LEVEL, "Settings is successfully updated!");
        } catch (IllegalAccessException | NullPointerException e) {
            LogUtils.error(ModuleLevel.SETTING_LEVEL, e);
        }
    }

    public void scanContexts(String directory) throws IOException {
        Path rootDirectory = Paths.get(directory);
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
        this.contexts = results;
        Properties properties = properties();
        try {
            String key = this.getClass().getDeclaredField("contexts").getAnnotation(PropValue.class).value();
            Objects.requireNonNull(properties).setProperty(key, String.join(", ", results));
        } catch (NoSuchFieldException | NullPointerException e) {
            LogUtils.log(ModuleLevel.SETTING_LEVEL, "Unknown error!!");
        }
    }
}
