package org.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class ResourcesFileUtils {

    public static String fileContents(String filename) {
        try {
            return new String(loadFile(filename));
        } catch (IOException ex) {
            throw new RuntimeException("Error while reading resource file: '%s'".formatted(filename), ex);
        }
    }

    public static byte[] loadResourceFile(String filename) throws IOException {

        final var classloader = Thread.currentThread().getContextClassLoader();
        final var file = new File(classloader.getResource(filename).getFile());

        try (FileInputStream input = new FileInputStream(file)) {
            return input.readAllBytes();
        }
    }

    public static byte[] loadFile(String filename) throws IOException {

        final var file = new File(filename);

        try (FileInputStream input = new FileInputStream(file)) {
            return input.readAllBytes();
        }
    }

}
