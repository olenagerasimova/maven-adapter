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

package com.artipie.maven.metadata;

import com.artipie.maven.artifact.Artifact;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

/**
 * Artifact metadata.
 *
 * Metadata is information about an artifact. It is a xml described in
 * http://maven.apache.org/ref/3.3.9/maven-repository-metadata/repository-metadata.html .
 *
 * @since 0.2
 *
 * @todo #57:30min Continue to implement metadata generation.
 *  Artifact metadata is generated according to Artifact versions and files.
 *  The implementation of metadata must read all files from all versions of
 *  some artifact and then generate a xml representing it. Please refer to
 *  http://maven.apache.org/ref/3.3.9/maven-repository-metadata/repository-metadata.html
 *  to metadata xml structure. Once finished, enable test at Metadata test.
 */
public interface Metadata {

    /**
     * Artifact metadata content.
     *
     * @return Artifact metadata.
     */
    Publisher<ByteBuffer> content();

    /**
     * Maven metadata implementation.
     *
     * @since 0.2
     */
    class Maven implements Metadata {

        /**
         * Artifact for metadata retrieval.
         */
        private final Artifact artifact;

        /**
         * Constructor.
         * @param artifact Artifact for metadata retrieval.
         */
        public Maven(final Artifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public Publisher<ByteBuffer> content() {
            throw new UnsupportedOperationException();
        }
    }
}
