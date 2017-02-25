package com.github.nwillc.reloader;

import com.github.nwillc.contracts.UtilityClassContract;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileUtilsTest extends UtilityClassContract {

    public static final String NEWEST = "./src/test/resources/foo-1.2.blah.jar";

    @Override
    public Class<?> getClassToTest() {
        return FileUtils.class;
    }

    @BeforeClass
    public static void setUp() {
        final Path path = Paths.get(NEWEST);
        path.toFile().setLastModified(System.currentTimeMillis());
    }

    @Test
    public void testToPattern() throws Exception {
        final String[][] paths = new String[][]{
                {"../libs/foo-1.0.jar", "foo.+\\.jar"},
                {"../libs/foo-1.0-SNAPSHOT.jar", "foo.+\\.jar"},
                {"../libs/foo-bar-1.0.jar", "foo-bar.+\\.jar"},
                {"../libs/foo-bar-1.0-Alpha1.jar", "foo-bar.+\\.jar"},
                {"../libs/foo-1.0RC2.bz2", "foo.+\\.bz2"}
        };

        for (String[] pair : paths) {
            assertThat(FileUtils.toPattern(Paths.get(pair[0]))).isEqualTo(pair[1]);
        }

    }

    @Test
    public void testLs() throws Exception {
        Stream<Path> files = FileUtils.ls(Paths.get("./src/test/resources"), ".*\\.jar");
        final List<Path> pathList = files.collect(Collectors.toList());
        assertThat(pathList).contains(
                Paths.get("./src/test/resources/foo-1.0.jar"),
                Paths.get("./src/test/resources/foo-1.1.jar"),
                Paths.get(NEWEST)
                );

        files = FileUtils.ls(Paths.get("./src/test/resources"), ".*blah\\.jar");
        assertThat(files.count()).isEqualTo(1);
    }

    @Test
    public void testNewest() throws Exception {
        final String newest = FileUtils.findNewest(Paths.get("./src/test/resources/foo-1.0.jar"));

        assertThat(newest).isNotNull();
        assertThat(newest).isEqualTo("./src/test/resources/foo-1.2.blah.jar");
    }
}