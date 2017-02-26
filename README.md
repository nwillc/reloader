
# Reloader

A java package to reload a running Java applications.

## Features

- Based on run time information, intelligently recreate the application's command line.
- Exit the application, invoking the recreated command line as new process.
- Execute shutdown code prior to exit.
- If the application was packaged as a single jar, it can determine if there is a newer
 version of the jar and use that instead, i.e. handle an update.
- Can be registered as a signal handler.

## Why?

It is designed for restarting and updating running applications.

## Example Use

The artifacts are on JCenter.
 
 [![Download](http://shields-nwillc.rhcloud.com/shield/jcenter?path=nwillc&package=reloader)](http://shields-nwillc.rhcloud.com/homepage/jcenter?path=nwillc&package=reloader)

Suppose you want your application to restart from the latest version when the process receives a `USR2` signal.
In your Java `main` simply:

```java
Reloader.onSignal("USR2");

```

That is it. Do you need shutdown code, but to run the same version?
 
```java 
 Reloader.onSignal("USR2", () -> { /* shutdiown code */ }, false);

```

Perhaps you don't want to base invocation on a signal, but to call reloader in an event handler:

```java
Reloader.restartApplication(() -> {/* shutdiown code */}, true);

```

## Credits

The origin of Reloader is Leo Lewis' article entitled [Programmatically Restart a Java Application](https://dzone.com/articles/programmatically-restart-java).

## See Also

- [License](LICENSE.md)
- [Javadoc](https://nwillc.github.io/reloader/javadoc)

