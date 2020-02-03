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

import com.artipie.maven.test.OptionalAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test class for {@link FileCoordinates}.
 * @since 0.1
 */
class FileCoordinatesTest {

    /**
     * Creates test instances.
     */
    private static final FileCoordinates.Parser PARSER =
        new FileCoordinates.Parser();

    @Test
    public void testEmptyClassifier() {
        OptionalAssertions.empty(
            PARSER.parse("group:artifact:1.0").getClassifier()
        );
    }

    @Test
    public void testPresentClassifier() {
        final var classifier = PARSER.parse("group:artifact:jar:sources:1.0")
            .getClassifier();
        OptionalAssertions.present(
            classifier,
            s -> Assertions.assertEquals("sources", s)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "'artifact-2.0.jar','org.group:artifact:jar:2.0'",
        "'artifact-1.0-sources.jar','org.group:artifact:jar:sources:1.0'"
    })
    public void testGetFileName(final String name, final String coordinates) {
        final var actual = PARSER.parse(coordinates).getFileName();
        Assertions.assertEquals(name, actual);
    }

    @ParameterizedTest
    @CsvSource({
        "'org/group/artifact/2.0/artifact-2.0.pom','org.group:artifact:pom:2.0'",
        "'org/group/artifact/1.0/artifact-1.0-sources.jar','org.group:artifact:jar:sources:1.0'"
    })
    public void testGetPath(final String path, final String coordinates) {
        final var actual = PARSER.parse(coordinates).getPath();
        Assertions.assertEquals(path, actual);
    }

    @Test
    public void testPathGeneral() {
        final var path = PARSER.path("org/group/artifact/2.0/artifact-2.0.pom");
        Assertions.assertAll(
            () -> Assertions.assertEquals("org.group", path.getGroupId()),
            () -> Assertions.assertEquals("artifact", path.getArtifactId()),
            () -> Assertions.assertEquals("2.0", path.getVersion()),
            () -> Assertions.assertEquals("pom", path.getExtension()),
            () -> OptionalAssertions.empty(path.getClassifier())
        );
    }

    @Test
    public void testPathClassifier() {
        final var path = PARSER.path("group/name/1.0/name-1.0-javadoc.jar");
        Assertions.assertAll(
            () -> Assertions.assertEquals("group", path.getGroupId()),
            () -> Assertions.assertEquals("name", path.getArtifactId()),
            () -> Assertions.assertEquals("1.0", path.getVersion()),
            () -> Assertions.assertEquals("jar", path.getExtension()),
            () -> OptionalAssertions.present(
                path.getClassifier(),
                c -> Assertions.assertEquals("javadoc", c)
            )
        );
    }

}
