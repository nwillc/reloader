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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Reloader {
    public static final String SUN_JAVA_COMMAND = "sun.java.command";

    private Reloader() {
    }

    public static void onSignal(final String signalName) {
        Signal sig = null;
        try {
            final Class<?> aClass = Class.forName(Signal.class.getCanonicalName());
            final Constructor<?> cons = aClass.getConstructor(String.class);
            sig = (Signal) cons.newInstance(signalName);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (sig != null) {
            Signal.handle(sig, signal -> {
                restartApplication(null);
            });
        }
    }

    static List<String> newCommandLine(List<String> vmArgs, List<String> cmdArgs) {
        final List<String> newCommandLine = new ArrayList<>();
        newCommandLine.add(System.getProperty("java.home") + "/bin/java");
        vmArgs.stream().filter(s -> !s.equals("-agentlib")).forEach(newCommandLine::add);
        if (cmdArgs.get(0).endsWith(".jar")) {
            newCommandLine.add("-jar");
        } else {
            newCommandLine.add("-cp");
            newCommandLine.add(System.getProperty("java.class.path"));
        }
        cmdArgs.forEach(newCommandLine::add);
        return newCommandLine;
    }

    public static void restartApplication(Runnable onExit) {
        try {
            final List<String> newCommandLine = newCommandLine(ManagementFactory.getRuntimeMXBean().getInputArguments(), Arrays.asList(System.getProperty(SUN_JAVA_COMMAND).split(" ")));
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
