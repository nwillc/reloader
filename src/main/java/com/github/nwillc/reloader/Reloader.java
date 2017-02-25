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

import org.pmw.tinylog.Logger;
import sun.misc.Signal;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.github.nwillc.reloader.FileUtils.findNewest;

/**
 * Utility class providing methods for reloading the current Java application.
 */
public final class Reloader {
    private static final String SUN_JAVA_COMMAND = "sun.java.command";
    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String JAVA_HOME = "java.home";
    private static final String BIN_JAVA = "/bin/java";
    private static final String AGENTLIB_CMD = "-agentlib";
    private static final String JAR_FILE_EXT = ".jar";
    private static final String JAR_CMD = "-jar";
    private static final String CLASS_PATH_CMD = "-cp";

    private Reloader() {
    }

    public static void onSignal(final String signalName) {
        onSignal(signalName, null, true);
    }

    /**
     * Set up a Un*x signal handler to call {@link #restartApplication(Runnable, boolean)} when the signal
     * is received.
     *
     * @param signalName Name of the signal.
     * @param onExit     A runnable to perform on exit.
     * @param withNewest If a jar, should we find the newest?
     */
    public static void onSignal(final String signalName, Runnable onExit, boolean withNewest) {
        Signal sig = new Signal(signalName);
        Signal.handle(sig, signal -> {
            restartApplication(onExit, withNewest);
        });
    }

    static List<String> newCommandLine(List<String> vmArgs, List<String> cmdArgs, boolean withUpdate) {
        final List<String> newCommandLine = new ArrayList<>();
        newCommandLine.add(System.getProperty(JAVA_HOME) + BIN_JAVA);
        vmArgs.stream().filter(s -> !s.equals(AGENTLIB_CMD)).forEach(newCommandLine::add);

        final Iterator<String> origCmdArgs = cmdArgs.iterator();
        if (cmdArgs.get(0).endsWith(JAR_FILE_EXT)) {
            newCommandLine.add(JAR_CMD);
            if (withUpdate) {
                try {
                    final Path jarPath = Paths.get(cmdArgs.get(0));
                    final String newest = findNewest(jarPath);
                    newCommandLine.add(newest);
                    origCmdArgs.next();
                } catch (IOException e) {
                    Logger.info("Failed determining newest version of jar: " + cmdArgs.get(0));
                }
            }
        } else {
            newCommandLine.add(CLASS_PATH_CMD);
            newCommandLine.add(System.getProperty(JAVA_CLASS_PATH));
        }

        origCmdArgs.forEachRemaining(newCommandLine::add);
        return newCommandLine;
    }

    /**
     * Restart the current application.  If a Runnable is provided, execute that first. If the application
     * was run from a single '-jar' jar, and withUpdate is true, look for the newest version of the jar.
     *
     * @param onExit     Code to execute on shutdown.
     * @param withUpdate Should an attempt be made to update to newest jar.
     */
    public static void restartApplication(final Runnable onExit, final boolean withUpdate) {
        try {
            final List<String> newCommandLine = newCommandLine(
                    ManagementFactory.getRuntimeMXBean().getInputArguments(),
                    Arrays.asList(System.getProperty(SUN_JAVA_COMMAND).split(" ")),
                    withUpdate);

            Logger.info("Restart: " + newCommandLine);

            // Set up the command line to be executed on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Runtime.getRuntime().exec(newCommandLine.toArray(new String[]{}));
                } catch (IOException e) {
                    Logger.error(e, "Failed to reload.");
                }
            }));

            // If a onExit was provided run it.
            if (onExit != null) {
                onExit.run();
            }

            // exit
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to restart the application", e);
        }
    }
}
