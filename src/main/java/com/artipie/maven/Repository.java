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

package com.artipie.maven;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

/**
 * General abstraction over Maven (remote) repository.
 *
 * @since 0.1
 * @Deprecated Use {@link com.artipie.maven.repository.Repository} instead.
 */
public interface Repository {

    /**
     * Downloads given artifact.
     * @param path Artifact URI path
     * @return File payload
     */
    Flow.Publisher<ByteBuffer> download(String path);

    /**
     * Uploads given artifact.
     * @param path Artifact URI path segment
     * @param content Artifact binary
     * @return Artifact metadata
     */
    Flow.Publisher<ArtifactMetadata> deploy(String path, Flow.Publisher<ByteBuffer> content);
}
