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

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Versions}.
 * @since 0.5
 */
class VersionsTest {

    @Test
    void comparesSimpleVersions() {
        MatcherAssert.assertThat(
            new Versions().compare("1", "2"),
            new IsEqual<>(-1)
        );
    }

    @Test
    void comparesMinorVersions() {
        MatcherAssert.assertThat(
            new Versions().compare("0.2", "0.21.2"),
            new IsEqual<>(-1)
        );
    }

    @Test
    void comparesSimpleWithSnapshot() {
        MatcherAssert.assertThat(
            new Versions().compare("1.0", "1.1-SNAPSHOT"),
            new IsEqual<>(-1)
        );
    }

    @Test
    void comparesSnapshotWithSimple() {
        MatcherAssert.assertThat(
            new Versions().compare("2.1-SNAPSHOT", "2.1.1"),
            new IsEqual<>(-1)
        );
    }

    @Test
    void comparesSnapshotWithSnapshot() {
        MatcherAssert.assertThat(
            new Versions().compare("1.0.1-SNAPSHOT", "1.2.1-SNAPSHOT"),
            new IsEqual<>(-1)
        );
    }

}
