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
package com.artipie.maven.http;

import com.artipie.asto.Key;
import java.util.Collections;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test case for {@link ArtifactHeaders}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ArtifactHeadersTest {

    @Test
    void addsChecksumAndEtagHeaders() {
        final String one = "one";
        final String two = "two";
        final String three = "three";
        MatcherAssert.assertThat(
            new MapOf<>(
                new ArtifactHeaders(
                    new Key.From("anything"),
                    new MapOf<>(
                        new MapEntry<>("sha1", one),
                        new MapEntry<>("sha256", two),
                        new MapEntry<>("sha512", three)
                    )
                )
            ),
            Matchers.allOf(
                Matchers.hasEntry("X-Checksum-sha1", one),
                Matchers.hasEntry("X-Checksum-sha256", two),
                Matchers.hasEntry("X-Checksum-sha512", three),
                Matchers.hasEntry("ETag", one)
            )
        );
    }

    @Test
    void addsContentDispositionHeader() {
        MatcherAssert.assertThat(
            new MapOf<>(
                new ArtifactHeaders(
                    new Key.From("artifact.jar"),
                    Collections.emptyNavigableMap()
                )
            ),
            Matchers.hasEntry("Content-Disposition", "attachment; filename=\"artifact.jar\"")
        );
    }

    @CsvSource({
        "target.jar,application/java-archive",
        "target.pom,application/x-maven-pom+xml",
        "target.xml,application/xml",
        "target.none,*"
    })
    @ParameterizedTest
    void addsContentTypeHeaders(final String target, final String mime) {
        MatcherAssert.assertThat(
            new MapOf<>(new ArtifactHeaders(new Key.From(target), Collections.emptyNavigableMap())),
            Matchers.hasEntry("Content-Type", mime)
        );
    }
}
