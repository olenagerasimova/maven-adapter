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

/**
 * Default interface impl.
 * @since 0.1
 * @deprecated Outdated due architectural changes in 0.2
 */
@Deprecated
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.DataClass"})
public final class DetachedMetadata implements ArtifactMetadata {
    /**
     * Artifact coordinates.
     */
    private final ArtifactCoordinates coordinates;

    /**
     * Relative /-delimited path to the artifact.
     */
    private final String path;

    /**
     * Binary size.
     */
    private final long size;

    /**
     * MD5 hex-encoded checksum.
     * @checkstyle MemberNameCheck (2 lines)
     */
    private final String md5;

    /**
     * SHA1 hex-encoded checksum.
     * @checkstyle MemberNameCheck (2 lines)
     */
    private final String sha1;

    /**
     * All args constructor.
     * @param coordinates Artifact coordinates
     * @param path Artifact path
     * @param size Binary size
     * @param md5 MD5 hex-encoded checksum.
     * @param sha1 SHA1 hex-encoded checksum
     * @checkstyle ParameterNameCheck (6 lines)
     * @checkstyle ParameterNumberCheck (4 lines)
     */
    public DetachedMetadata(
        final ArtifactCoordinates coordinates, final String path,
        final long size, final String md5, final String sha1
    ) {
        this.coordinates = coordinates;
        this.path = path;
        this.size = size;
        this.md5 = md5;
        this.sha1 = sha1;
    }

    @Override
    public ArtifactCoordinates coordinates() {
        return this.coordinates;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public long size() {
        return this.size;
    }

    // @checkstyle MemberNameCheck 3 lines)
    @Override
    public String md5() {
        return this.md5;
    }

    // @checkstyle MemberNameCheck 3 lines)
    @Override
    public String sha1() {
        return this.sha1;
    }

}
