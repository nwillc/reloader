package com.github.nwillc.reloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FileUtils {
    private FileUtils() {
    }

    public static Stream<Path> ls(Path path, String pattern) throws IOException {
        final Pattern filePattern = Pattern.compile(pattern);
        return Files.walk(path).filter(p -> filePattern.matcher(p.getFileName().toString()).matches());
    }

    public static String findNewest(Path path, String pattern) throws IOException {
        return ls(path, pattern).sorted((o1, o2) -> {
            try {
                final FileTime t1 = Files.getLastModifiedTime(o1);
                final FileTime t2 = Files.getLastModifiedTime(o2);
                return t2.compareTo(t1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }).findFirst().get().getFileName().toString();
    }
}
