/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.maven.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adapts {@link Path} to {@link AutoCloseable} interface, quietly deleting on close.
 * @since 0.1
 * @deprecated Outdated due architectural changes in 0.2
 */
@Deprecated
public final class AutoCloseablePath implements AutoCloseable {

    /**
     * Actual path.
     */
    private final Path path;

    /**
     * All arg constructor.
     * @param path A path to instance to wrap over.
     */
    public AutoCloseablePath(final Path path) {
        this.path = path;
    }

    @Override
    public void close() throws Exception {
        try {
            Files.deleteIfExists(this.path);
        } catch (final IOException ex) {
            throw new FileCleanupException(ex);
        }
    }

    /**
     * Returns internal Path instance.
     * @return Path instance
     */
    public Path unwrap() {
        return this.path;
    }

    /**
     * Encapsulates parent directory logic to not to expose it as a raw {@link Path}.
     * @since 0.1
     */
    public static class Parent {

        /**
         * Root directory.
         */
        private final Path dir;

        /**
         * All args constructor.
         * @param dir Root directory
         */
        public Parent(final Path dir) {
            this.dir = dir;
        }

        /**
         * Resolves given path (if it's relative path) as a child of the root.
         * Creates parent directories if needed
         * @param path A child path.
         * @return AutoCloseablePath instance
         * @throws IOException Failed to create parent directories
         */
        public AutoCloseablePath resolve(final Path path) throws IOException {
            final var file = this.dir.resolve(path);
            Files.createDirectories(file.getParent());
            return new AutoCloseablePath(file);
        }

        /**
         * Resolves given path (if it's relative path) as a child of the root.
         * @param path Path as a string
         * @return AutoCloseablePath instance
         * @throws IOException Failed to create parent directories
         */
        public AutoCloseablePath resolve(final String path) throws IOException {
            return this.resolve(Paths.get(path));
        }
    }

}
