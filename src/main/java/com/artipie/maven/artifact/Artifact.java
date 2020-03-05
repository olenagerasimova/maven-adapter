package com.artipie.maven.artifact;

/**
 * Maven artifact abstraction.
 */
public interface Artifact {

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
