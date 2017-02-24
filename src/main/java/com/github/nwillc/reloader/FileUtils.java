/*
 * Copyright (c) 2017, nwillc@gmail.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package com.github.nwillc.reloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FileUtils {

    private static final String FILENAME_PARTS = "^([^-.]+)(.*)(\\.[^.]+)$";
    private static final String WITHOUT_VERSION = "$1.+\\\\$3";

    private FileUtils() {
    }

    public static String toPattern(Path path) {
        return path.getFileName().toString().replaceAll(FILENAME_PARTS, WITHOUT_VERSION);
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
