package com.github.nwillc.reloader;

import com.github.nwillc.contracts.UtilityClassContract;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class FileFinderTest extends UtilityClassContract {

    @Override
    public Class<?> getClassToTest() {
        return FileFinder.class;
    }

    @Test
    public void testLs() throws Exception {
        Stream<Path> files = FileFinder.ls(Paths.get("./src/test/resources"), ".*\\.txt");
        assertThat(files.count()).isEqualTo(3);

        files = FileFinder.ls(Paths.get("./src/test/resources"), ".*blah\\.txt");
        assertThat(files.count()).isEqualTo(1);
    }

    @Test
    public void testFileFinder() throws Exception {

        final String newest = FileFinder.findNewest(Paths.get("./src/test/resources"), "foo-.*\\.txt");

        assertThat(newest).isNotNull();
        assertThat(newest).isEqualTo("foo-1.2.blah.txt");
    }
}