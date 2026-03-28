package org.tanchee.dam.util;

import java.io.InputStream;

public interface ResourceLoadingClass {
    default InputStream streamResource(String resourceName) {
        return getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}
