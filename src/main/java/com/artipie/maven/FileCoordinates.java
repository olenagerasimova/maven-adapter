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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

/**
 * Identifies a single artifact file split into meaningful parts.
 * @since 0.1
 */
public final class FileCoordinates implements ArtifactCoordinates {

    /**
     * GroupId.
     */
    // @checkstyle JavadocLocationCheck (2 lines)
    // @checkstyle MemberNameCheck (1 line)
    private final String groupId;

    /**
     * ArtifactId.
     */
    // @checkstyle JavadocLocationCheck (2 lines)
    // @checkstyle MemberNameCheck (1 line)
    private final String artifactId;

    /**
     * Version.
     */
    private final String version;

    /**
     * Nullable, usually it's "javadoc" or "sources".
     */
    private final String classifier;

    /**
     * File extension.
     */
    private final String extension;

    /**
     * Use {@link Parser}.
     * @param groupId GroupId
     * @param artifactId ArtifactId
     * @param version Version
     * @param classifier Classifier
     * @param extension Extension
     */
    // @checkstyle JavadocLocationCheck (3 lines)
    // @checkstyle ParameterNumberCheck (3 lines)
    // @checkstyle ParameterNameCheck (2 lines)
    private FileCoordinates(final String groupId, final String artifactId, final String version,
        final String classifier, final String extension) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
    }

    /**
     * Classifier is not mandatory.
     * @return Optional of a classifier. Its value cannot be blank.
     */
    public Optional<String> getClassifier() {
        return Optional.ofNullable(this.classifier)
            .filter(s -> !s.isBlank());
    }

    @Override
    public String toString() {
        final var tsb = new StringBuilder(this.groupId)
            .append(':').append(this.artifactId)
            .append(':').append(this.extension);
        if (this.classifier != null) {
            tsb.append(':').append(this.classifier);
        }
        tsb.append(':').append(this.version);
        return tsb.toString();
    }

    /**
     * Returns full (local) path identifying the artifact.
     * @return Relative slash-delimited path
     */
    public String getPath() {
        final var group = this.groupId.replace('.', '/');
        return String.join("/", group, this.artifactId, this.version, this.getFileName());
    }

    /**
     * Returns only file name of {@link #getPath}.
     * @return File name part
     */
    public String getFileName() {
        final var name = new StringBuilder(this.artifactId)
            .append("-").append(this.version);
        this.getClassifier()
            .ifPresent(s -> name.append("-").append(s));
        name.append(".").append(this.extension);
        return name.toString();
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * File extension.
     * @return File extension
     */
    public String getExtension() {
        return this.extension;
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final var that = (FileCoordinates) obj;
        return Objects.equals(this.groupId, that.groupId)
            && Objects.equals(this.artifactId, that.artifactId)
            && Objects.equals(this.version, that.version)
            && Objects.equals(this.classifier, that.classifier)
            && Objects.equals(this.extension, that.extension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.groupId, this.artifactId, this.version, this.classifier, this.extension
        );
    }

    /**
     * Parses and validates given string.
     * @since 0.1
     */
    public static class Parser {
        /**
         * Regex pattern to match
         * {@code <groupId>:<artifactId>:<extension>[:<classifier>]:<version>}.
         */
        private static final Pattern PATTERN =
            Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

        /**
         * Validates given string.
         * @param coords Artifact coords matching {@value #PATTERN}
         * @return FileCoordinates instance
         * @throws IllegalArgumentException if coords does not match specified pattern
         * @checkstyle NonStaticMethodCheck (2 lines)
         */
        public FileCoordinates parse(final String coords) {
            if (coords == null || coords.isBlank()) {
                throw new IllegalArgumentException("coords should not be blank");
            }
            final var matcher = PATTERN.matcher(coords);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(String.format("Invalid format %s", coords));
            }
            // @checkstyle LocalFinalVariableNameCheck (2 lines)
            final var groupId = matcher.group(1);
            // @checkstyle LocalFinalVariableNameCheck (2 lines)
            final var artifactId = matcher.group(2);
            final var extension = matcher.group(4);
            // @checkstyle MagicNumberCheck (1 line)
            final var classifier = Optional.ofNullable(matcher.group(6))
                .filter(s -> !s.isBlank())
                .orElse(null);
            final var version = matcher.group(7);
            return new FileCoordinates(groupId, artifactId, version, classifier, extension);
        }

        /**
         * Validates given string.
         * <p>
         * Given path should follow convention
         * org/group/artifact/version/artifact-version[-classifier].extension
         * <p>
         * It starts parsing by splitting the path by '/' and picking
         * segments from the end - name, version, artifact and then
         * recreates groupId
         * @param path URI /-delimited path
         * @return FileCoordinates instance
         * @checkstyle MagicNumberCheck (30 lines)
         * @checkstyle NonStaticMethodCheck (3 lines)
         */
        public FileCoordinates path(final String path) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("path should not be blank");
            }
            final String[] split = Arrays.stream(path.split("/"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
            if (split.length < 4) {
                throw new IllegalArgumentException(String.format("Invalid path %s", path));
            }
            final var version = split[split.length - 2];
            // @checkstyle LocalFinalVariableNameCheck (2 lines)
            final var artifactId = split[split.length - 3];
            // @checkstyle LocalFinalVariableNameCheck (2 lines)
            final var groupId = String.join(
                ".",
                Arrays.copyOfRange(split, 0, split.length - 3)
            );
            final var name = split[split.length - 1];
            final var extension = FilenameUtils.getExtension(name);
            final var names = FilenameUtils.removeExtension(name).split("-");
            var classifier = "";
            if (names.length == 3) {
                classifier = names[2];
            }
            return new FileCoordinates(groupId, artifactId, version, classifier, extension);
        }
    }

}
