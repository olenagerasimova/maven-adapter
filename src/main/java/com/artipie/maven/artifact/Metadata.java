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

package com.artipie.maven.artifact;

/**
 * Artifact file metadata.
 * @since 0.1
 * @todo #54:30min Implement Metadata generation. Metadata must be generated through the use of `Storage` and `Key`
 *  interfaces from artipie/asto.
 */
public interface Metadata {
    /**
     * Artifact coordinates.
     * @return Artifact coordinates
     */
    Coordinates coordinates();

    /**
     * Artifact path.
     * @return Artifact path
     */
    String path();

    /**
     * Artifact binary size.
     * @return Artifact binary size
     */
    long size();

    /**
     * MD5 hex-encoded checksum.
     * @return MD5 hex-encoded checksum
     * @checkstyle MethodNameCheck (2 lines)
     */
    String md5();

    /**
     * SHA1 hex-encoded checksum.
     * @return SHA1 hex-encoded checksum
     * @checkstyle MethodNameCheck (2 lines)
     */
    String sha1();
}
