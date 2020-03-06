package com.artipie.maven.artifact;

import com.artipie.asto.Storage;

/**
 * Maven artifact abstraction.
 */
public interface Artifact {

    /**
     * Artifact storage.
     * @return Artifact abstract storage.
     */
    Storage storage();

    /**
     * Artifact coordinates.
     * @return Artifact coordinates.
     */
    Coordinates coordinates();

    /**
     * Artifact metadata.
     * @return Artifact metadata.
     */
    Metadata metadata();
}
