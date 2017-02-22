package com.github.nwillc.reloader;

import com.github.nwillc.contracts.UtilityClassContract;

/**
 *
 */
public class ReloaderTest extends UtilityClassContract {
    @Override
    public Class<?> getClassToTest() {
        return Reloader.class;
    }
}