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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts {@link Path} to {@link AutoCloseable} interface, quietly deleting on close.
 * @since 0.1
 */
public final class AutoCloseablePath implements DelegatingPathSupport, AutoCloseable {
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AutoCloseablePath.class);

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
            LOG.warn(String.format("with %s", this.path), ex);
        }
    }

    @Override
    public Path delegate() {
        return this.path;
    }

    /**
     * A convenient helper method for {@link Files#size(Path)}.
     * @return File size
     */
    public long size() {
        try {
            return Files.size(this.path);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * A convenient helper method for
     * {@link Files#copy(java.io.InputStream, java.nio.file.Path, java.nio.file.CopyOption...)}.
     * @param payload InputStream to read from
     * @return Bytes written
     */
    public long copyFrom(final InputStream payload) {
        try {
            return Files.copy(payload, this.path);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Encapsulates parent directory logic to not to expose it as a raw {@link Path}.
     * @since 0.1
     */
    public static class Root {
        /**
         * Root directory.
         */
        private final Path dir;

        /**
         * All args constructor.
         * @param dir Root directory
         */
        public Root(final Path dir) {
            this.dir = dir;
        }

        /**
         * Resolves given path (if it's relative path) as a child of the root.
         * Creates parent directories if needed
         * @param path A child path.
         * @return AutoCloseablePath instance
         */
        public Path resolve(final Path path) {
            try {
                Files.createDirectories(path.getParent());
                return new AutoCloseablePath(this.dir.resolve(path));
            } catch (final IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        /**
         * Resolves given path (if it's relative path) as a child of the root.
         * @param path Path as a string
         * @return AutoCloseablePath instance
         */
        public Path resolve(final String path) {
            return this.resolve(Paths.get(path));
        }
    }
}
