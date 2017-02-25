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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Reloader {
    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
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
        }
        cmdArgs.stream().forEach(newCommandLine::add);
        return newCommandLine;
    }

    public static void restartApplication(Runnable runBeforeRestart) {
        try {
            final List<String> newCommandLine = newCommandLine(ManagementFactory.getRuntimeMXBean().getInputArguments(), Arrays.asList(System.getProperty(SUN_JAVA_COMMAND).split(" ")));
//            // java binary
//            String java = System.getProperty("java.home") + "/bin/java";
//
//            // vm arguments
//
//            StringBuilder vmArgsOneLine = new StringBuilder();
//            for (String arg : vmArguments) {
//                // if it's the agent argument : we ignore it otherwise the
//                // address of the old application and the new one will be in conflict
//                if (!arg.contains("-agentlib")) {
//                    vmArgsOneLine.append(arg);
//                    vmArgsOneLine.append(" ");
//                }
//            }
//
//            // init the command to execute, add the vm args
//            final StringBuffer cmd = new StringBuffer(java + " " + vmArgsOneLine);
//
//            // program main and program arguments
//            String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
//
//            // program main is a jar
//            if (mainCommand[0].endsWith(".jar")) {
//                // if it's a jar, add -jar mainJar
//                cmd.append("-jar ").append(new File(mainCommand[0]).getPath());
//            } else {
//                // else it's a .class, add the classpath and mainClass
//                cmd.append("-cp \"").append(System.getProperty("java.class.path")).append("\" ").append(mainCommand[0]);
//            }
//
//            // finally add program arguments
//            for (int i = 1; i < mainCommand.length; i++) {
//                cmd.append(" ");
//                cmd.append(mainCommand[i]);
//            }

            Logger.info("Restart: " + newCommandLine);
            // execute the command in a shutdown hook, to be sure that all the
            // resources have been disposed before restarting the application
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Runtime.getRuntime().exec(newCommandLine.toArray(new String[]{}));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            // execute some custom code before restarting
            if (runBeforeRestart != null) {
                runBeforeRestart.run();
            }

            // exit
            System.exit(0);
        } catch (Exception e) {
            // something went wrong
            throw new RuntimeException("Error while trying to restart the application", e);
        }
    }
}
