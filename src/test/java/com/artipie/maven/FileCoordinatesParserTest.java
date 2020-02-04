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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link FileCoordinates}.
 * @since 0.1
 */
class FileCoordinatesParserTest {

    /**
     * Happy-path test param.
     */
    private final FileCoordinatesParser parser = FileCoordinatesParser.splitting(
        "org/group/example/1.0/example-1.0-classifier.jar"
    );

    @ParameterizedTest
    @ValueSource(strings = {"", "org", "version/file.jar", "group/version/file.jar"})
    public void testFailing(final String param) {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> FileCoordinatesParser.splitting(param)
        );
    }

    @Test
    public void testNull() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> FileCoordinatesParser.splitting(null)
        );
    }

    @Test
    public void testGroupId() throws Exception {
        Assertions.assertEquals("org.group", this.parser.groupId());
    }

    @Test
    public void testArtifactId() throws Exception {
        Assertions.assertEquals("example", this.parser.artifactId());
    }

    @Test
    public void testVersion() throws Exception {
        Assertions.assertEquals("1.0", this.parser.version());
    }

    @Test
    public void testClassifier() throws Exception {
        Assertions.assertEquals("classifier", this.parser.classifier());
    }

    @Test
    public void testClassifierEmpty() throws Exception {
        Assertions.assertEquals(
            "",
            FileCoordinatesParser.splitting(
                "group/example/1.0/example-1.0.jar"
            ).classifier()
        );
    }
}
