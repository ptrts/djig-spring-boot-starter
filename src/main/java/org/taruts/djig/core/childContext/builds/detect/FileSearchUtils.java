package org.taruts.djig.core.childContext.builds.detect;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSearchUtils {

    public static boolean fileExists(File directory, List<String> fileNames) {
        return fileExists(directory, fileNames.toArray(String[]::new));
    }

    public static boolean fileExists(File directory, String... fileNames) {
        return Stream
                .of(fileNames)
                .anyMatch(fileName ->
                        fileExists(directory, fileName)
                );
    }

    private static boolean fileExists(File directory, String fileName) {
        return FileUtils.getFile(directory, fileName).exists();
    }

    public static String findFile(File directory, String... fileNames) {
        return findFiles(directory, fileNames)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static List<String> findFiles(File directory, String... fileNames) {
        return Stream
                .of(fileNames)
                .map(fileName -> findOneFile(directory, fileName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static String findOneFile(File directory, String fileName) {
        File file = FileUtils.getFile(directory, fileName);
        return file.exists() ? fileName : null;
    }
}
