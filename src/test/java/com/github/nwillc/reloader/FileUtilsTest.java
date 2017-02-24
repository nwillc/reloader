package com.github.nwillc.reloader;

import com.github.nwillc.contracts.UtilityClassContract;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileUtilsTest extends UtilityClassContract {

    @Override
    public Class<?> getClassToTest() {
        return FileUtils.class;
    }

    @Test
    public void testToPattern() throws Exception {
       assertThat(FileUtils.toPattern(Paths.get("../libs/foo-1.0-standard.jar"))).isEqualTo("foo.+\\.jar");
    }

    @Test
    public void testLs() throws Exception {
        Stream<Path> files = FileUtils.ls(Paths.get("./src/test/resources"), ".*\\.jar");
        assertThat(files.count()).isEqualTo(3);

        files = FileUtils.ls(Paths.get("./src/test/resources"), ".*blah\\.jar");
        assertThat(files.count()).isEqualTo(1);
    }

    @Test
    public void testFileFinder() throws Exception {
        String pattern = FileUtils.toPattern(Paths.get("foo.jar"));
        final String newest = FileUtils.findNewest(Paths.get("./src/test/resources"), pattern);

        assertThat(newest).isNotNull();
        assertThat(newest).isEqualTo("foo-1.2.blah.jar");
    }
}