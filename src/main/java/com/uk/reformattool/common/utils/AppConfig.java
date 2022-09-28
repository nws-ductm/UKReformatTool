package com.uk.reformattool.common.utils;

import com.uk.reformattool.common.annotations.PropValue;
import com.uk.reformattool.common.module.AbstractModuleHandler;
import com.uk.reformattool.common.module.FlowType;
import lombok.Data;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class AppConfig {

    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;

    @PropValue("application.target.directory")
    private String rootDirectory;

    @PropValue("application.temp.directory")
    private String tempDirectory;

    @PropValue("application.temp.prefix")
    private String tempPrefix;

    @PropValue("application.settings.mode")
    private int mode;

    @PropValue("application.settings.scan.suffixes")
    private String scanSuffixes;

    @PropValue("application.settings.scanContent")
    private boolean scanContent;

    @PropValue("application.settings.reformatFile")
    private boolean reformatFile;

    public static void init() {
        AppConfig.initMetaFile(AbstractModuleHandler.class);

        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            instance = new AppConfig();
            for (Field field : AppConfig.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(PropValue.class)) {
                    String value = properties.getProperty(field.getAnnotation(PropValue.class).value());
                    Class<?> c = field.getType();
                    if (int.class.isAssignableFrom(c) || Integer.class.isAssignableFrom(c)) {
                        field.setInt(instance, Integer.parseInt(value));
                    } else if (boolean.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c)) {
                        field.setBoolean(instance, Boolean.parseBoolean(value));
                    } else {
                        field.set(instance, value);
                    }
                }
            }
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static AppConfig getInstance() {
        return instance;
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

    public List<String> getSuffixes() {
        return Arrays.asList(instance.scanSuffixes.split(",\\s*"));
    }
}
