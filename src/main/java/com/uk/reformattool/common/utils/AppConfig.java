package com.uk.reformattool.common.utils;

import com.uk.reformattool.common.module.AbstractModuleHandler;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class AppConfig {

    private static Properties properties;

    public static void init() {
        AppConfig.initMetaFile(AbstractModuleHandler.class);
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String rootDirectory() {
        return properties.getProperty("application.target.directory");
    }

    public static List<String> suffixes() {
        String suffixes = properties.getProperty("application.settings.scan.suffixes");
        return Arrays.asList(suffixes.split(",\\s*"));
    }

    @SneakyThrows
    public static <T> void initMetaFile(Class<T> c) {
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
}
