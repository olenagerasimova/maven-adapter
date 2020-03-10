package com.artipie.maven.artifact;

/**
 * Artifact collection with the same groupId and artifactId maven attributes.
 *
 * The concept is that we have many Libraries in a Maven Repository, each library categorized by its groupId:artifactId
 * names. Inside each library we have the Artifacts, which are a set of all files of a given library in a specific
 * version.
 *
 * @todo #54:30min Implement a Library backed up by asto Storage
 *  Library is a collection of Artifacts. Implement a Library which data is backed by a asto Storage implementation.
 *
 * @since 0.2
 */
public interface Library extends Iterable<Artifact> {

    /**
     * Group id in maven repository.
     * @return Group id in maven repository.
     */
    String groupId();

    /**
     * Artifact id of the library.
     * @return artifact id.
     */
    String artifactId();
}
