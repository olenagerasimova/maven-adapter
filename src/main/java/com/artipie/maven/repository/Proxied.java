package com.artipie.maven.repository;

import org.apache.maven.repository.Proxy;

/**
 * Proxyed repository.
 */
public interface Proxied {

    /**
     * Repository proxy.
     * @return Repository proxy.
     */
    Proxy proxy();
}
