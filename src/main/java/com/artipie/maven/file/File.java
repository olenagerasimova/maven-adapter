package com.artipie.maven.file;

/**
 * Repository file abstraction.
 *
 * Represents a file into a maven repository.
 *
 * @todo #54:30min Implement file storage using artipie/asto Storage.
 *  Files must be stored somewhere. Use artipie/asto to provide an storage for
 *  the repository files.
 *
 * @since 0.2
 */
public interface File {

    /**
     * File contents.
     *
     * @return File contents.
     */
     byte[] content();

    /**
     * File name.
     *
     * @return File name.
     */
    String name();
}
