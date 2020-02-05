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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * Provides default methods for concrete {@link Path} implementations.
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface DelegatingPathSupport extends Path {

    /**
     * Unwraps current path.
     * @return Internal path instance.
     */
    Path delegate();

    @Override
    default FileSystem getFileSystem() {
        return this.delegate().getFileSystem();
    }

    @Override
    default boolean isAbsolute() {
        return this.delegate().isAbsolute();
    }

    @Override
    default Path getRoot() {
        return this.delegate().getRoot();
    }

    @Override
    default Path getFileName() {
        return this.delegate().getFileName();
    }

    @Override
    default Path getParent() {
        return this.delegate().getParent();
    }

    @Override
    default int getNameCount() {
        return this.delegate().getNameCount();
    }

    @Override
    default Path getName(final int index) {
        return this.delegate().getName(index);
    }

    // @checkstyle ParameterNameCheck (2 lines)
    @Override
    default Path subpath(final int beginIndex, final int endIndex) {
        return this.delegate().subpath(beginIndex, endIndex);
    }

    @Override
    default boolean startsWith(final Path other) {
        return this.delegate().startsWith(other);
    }

    @Override
    default boolean endsWith(final Path other) {
        return this.delegate().endsWith(other);
    }

    @Override
    default Path normalize() {
        return this.delegate().normalize();
    }

    @Override
    default Path resolve(final Path other) {
        return this.delegate().resolve(other);
    }

    @Override
    default Path relativize(final Path other) {
        return this.delegate().relativize(other);
    }

    @Override
    default URI toUri() {
        return this.delegate().toUri();
    }

    @Override
    default Path toAbsolutePath() {
        return this.delegate().toAbsolutePath();
    }

    @Override
    default Path toRealPath(final LinkOption... options) throws IOException {
        return this.delegate().toRealPath(options);
    }

    @Override
    default WatchKey register(
        final WatchService watcher, final WatchEvent.Kind<?>[] events,
        final WatchEvent.Modifier... modifiers
    ) throws IOException {
        return this.delegate().register(watcher, events, modifiers);
    }

    @Override
    default int compareTo(final Path other) {
        return this.delegate().compareTo(other);
    }
}
