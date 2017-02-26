package com.github.nwillc.reloader;

import com.github.nwillc.contracts.UtilityClassContract;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ReloaderTest extends UtilityClassContract {


    @Override
    public Class<?> getClassToTest() {
        return Reloader.class;
    }

    @Test
    public void getProcessPid() throws Exception {
        final int pid = Reloader.getProcessPid();
        assertThat(pid).isGreaterThan(1);
    }

    @Test
    public void testNewCommandLine() throws Exception {
        final List<String> vmArgs = Arrays.asList("-Xmx100M");
        final List<String> cmdArgs = Arrays.asList("src/test/resources/foo-1.0.jar", "-p", "8080");
        final List<String> newCommandLine = Reloader.newCommandLine(vmArgs, cmdArgs, true);

        assertThat(newCommandLine.size()).isEqualTo(vmArgs.size() + cmdArgs.size() + 2);
        assertThat(newCommandLine.get(0)).endsWith("/bin/java");
        assertThat(newCommandLine).contains("-jar");
        assertThat(newCommandLine).contains("src/test/resources/foo-1.2.blah.jar");
        assertThat(newCommandLine).contains(vmArgs.toArray(new String[]{}));
        assertThat(newCommandLine).contains("-p", "8080");
    }

    @Test
    public void testOnSignal() throws Exception {
        Reloader.onSignal("USR2");
    }
}