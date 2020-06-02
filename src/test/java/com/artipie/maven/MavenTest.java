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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.memory.InMemoryStorage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Maven} class.
 *
 * @since 0.4
 * @checkstyle MethodNameCheck (500 lines)
 */
public class MavenTest {

    /**
     * Com name.
     */
    public static final String COM = "com";

    /**
     * Artipie name.
     */
    public static final String ARTIPIE = "artipie";

    /**
     * Maven name.
     */
    public static final String MAVEN = "maven";

    /**
     * Unsupported algorithm checksum file name.
     */
    public static final String UNSUPPORTED = "maven-metadata.xml.unsupported";

    @Test
    @Disabled
    public void generateValidMd5Checksum() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        new Maven(storage)
            .update(new Key.From(MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN))
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            storage.exists(
                new Key.From(
                    MavenTest.COM,
                    MavenTest.ARTIPIE,
                    MavenTest.MAVEN,
                    "maven-metadata.xml.md5"
                )
            ),
            new IsEqual<>(true)
        );
    }

    @Test
    @Disabled
    public void generateValidSha1Checksum() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        new Maven(storage)
            .update(
                new Key.From(MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN)
            )
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            storage.exists(
                new Key.From(
                    MavenTest.COM,
                    MavenTest.ARTIPIE,
                    MavenTest.MAVEN,
                    "maven-metadata.xml.sha1"
                )
            ),
            new IsEqual<>(true)
        );
    }

    @Test
    @Disabled
    public void generateValidSha256Checksum() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        new Maven(storage)
            .update(
                new Key.From(MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN)
            )
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            storage.exists(
                new Key.From(
                    MavenTest.COM,
                    MavenTest.ARTIPIE,
                    MavenTest.MAVEN,
                    "maven-metadata.xml.sha256"
                )
            ),
            new IsEqual<>(true)
        );
    }

    @Test
    @Disabled
    public void generateValidSha512Checksum() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        new Maven(storage)
            .update(
                new Key.From(MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN)
            )
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            storage.exists(
                new Key.From(
                    MavenTest.COM,
                    MavenTest.ARTIPIE,
                    MavenTest.MAVEN,
                    "maven-metadata.xml.sha512"
                )
            ),
            new IsEqual<>(true)
        );
    }

    @Test
    @Disabled
    public void removeUnsupportedAlgorithmChecksum() throws Exception {
        final InMemoryStorage storage = new InMemoryStorage();
        storage.save(
            new Key.From(
                MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN, MavenTest.UNSUPPORTED
            ),
            new Content.From("anything".getBytes())
        );
        new Maven(storage)
            .update(new Key.From(MavenTest.COM, MavenTest.ARTIPIE, MavenTest.MAVEN))
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            storage.exists(
                new Key.From(
                    MavenTest.COM,
                    MavenTest.ARTIPIE,
                    MavenTest.MAVEN,
                    MavenTest.UNSUPPORTED
                )
            ),
            new IsEqual<>(false)
        );
    }
}
