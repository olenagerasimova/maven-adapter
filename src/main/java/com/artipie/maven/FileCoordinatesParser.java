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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Parses and validates given string.
 * @since 0.1
 */
final class FileCoordinatesParser {

    /**
     * Path parts count.
     */
    private static final int PARTS_COUNT = 4;

    /**
     * Path parts.
     */
    private final String[] parts;

    /**
     * Creates a new instance, validating input string.
     * @param path An URI path
     * @throws IllegalArgumentException in case of invalid format
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    FileCoordinatesParser(final String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path should not be blank");
        }
        this.parts = StringUtils.removeStart(path, "/")
            .split("/");
        if (Arrays.stream(this.parts).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("path should not contain blank parts");
        }
        if (this.parts.length < FileCoordinatesParser.PARTS_COUNT) {
            throw new IllegalArgumentException(
                String.format(
                    "path should contain at least %d slash-delimited parts",
                    FileCoordinatesParser.PARTS_COUNT
                )
            );
        }
    }

    /**
     * GroupId.
     * @return Returns groupId part
     * @checkstyle MagicNumberCheck (5 lines)
     */
    String groupId() {
        return String.join(
            ".",
            Arrays.copyOfRange(this.parts, 0, this.parts.length - 3)
        );
    }

    /**
     * Filename.
     * @return Returns filename part
     * @checkstyle MagicNumberCheck (3 lines)
     */
    String name() {
        return this.parts[this.parts.length - 1];
    }

    /**
     * Version.
     * @return Returns version part
     * @checkstyle MagicNumberCheck (3 lines)
     */
    String version() {
        return this.parts[this.parts.length - 2];
    }

    /**
     * ArtifactId.
     * @return Returns artifactId part
     * @checkstyle MagicNumberCheck (3 lines)
     */
    String artifactId() {
        return this.parts[this.parts.length - 3];
    }

    /**
     * Classifier.
     * @return Returns classifier part
     */
    String classifier() {
        final var names = FilenameUtils.removeExtension(this.name())
            .split("-");
        var classifier = "";
        // @checkstyle MagicNumberCheck (3 lines)
        if (names.length == 3) {
            classifier = names[2];
        }
        return classifier;
    }

    /**
     * File extension.
     * @return Returns file extension part
     */
    String extension() {
        return FilenameUtils.getExtension(this.name());
    }
}
