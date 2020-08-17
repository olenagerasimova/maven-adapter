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

package com.artipie.maven.asto;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.ext.KeyLastPart;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.nio.charset.StandardCharsets;
import org.cactoos.list.ListOf;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AstoMaven} class.
 *
 * @since 0.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AstoMavenTest {

    /**
     * Upload key.
     */
    public static final Key UPLOAD = new Key.From(".update/com/test/logger");

    /**
     * Package key.
     */
    public static final Key PACKAGE = new Key.From("com/test/logger");

    /**
     * Test storage.
     */
    private Storage storage;

    @BeforeEach
    void init() {
        this.storage = new InMemoryStorage();
        new TestResource("maven-metadata.xml.example")
            .saveTo(this.storage, new Key.From(AstoMavenTest.UPLOAD, "maven-metadata.xml"));
    }

    @Test
    void generatesMetadata() throws InterruptedException {
        final BlockingStorage bsto = new BlockingStorage(this.storage);
        bsto.save(new Key.From(AstoMavenTest.PACKAGE, "0.8/artifact-0.8.jar"), new byte[]{});
        bsto.save(new Key.From(AstoMavenTest.PACKAGE, "0.9/artifact-0.9.jar"), new byte[]{});
        new AstoMaven(this.storage).update(AstoMavenTest.UPLOAD, AstoMavenTest.PACKAGE)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            new XMLDocument(
                this.storage.value(new Key.From(AstoMavenTest.UPLOAD, "maven-metadata.xml"))
                    .thenCompose(content -> new PublisherAs(content).string(StandardCharsets.UTF_8))
                    .join()
            ),
            new AllOf<>(
                new ListOf<Matcher<? super XML>>(
                    // @checkstyle LineLengthCheck (20 lines)
                    XhtmlMatchers.hasXPath("/metadata/groupId[text() = 'com.test']"),
                    XhtmlMatchers.hasXPath("/metadata/artifactId[text() = 'logger']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/latest[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/release[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.8']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.9']"),
                    XhtmlMatchers.hasXPath("metadata/versioning/versions[count(//version) = 3]"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/lastUpdated")
                )
            )
        );
    }

    @Test
    void generatesMetadataForFirstArtifact() {
        new AstoMaven(this.storage).update(AstoMavenTest.UPLOAD, AstoMavenTest.PACKAGE)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            new XMLDocument(
                this.storage.value(new Key.From(AstoMavenTest.UPLOAD, "maven-metadata.xml"))
                    .thenCompose(content -> new PublisherAs(content).string(StandardCharsets.UTF_8))
                    .join()
            ),
            new AllOf<>(
                new ListOf<Matcher<? super XML>>(
                    XhtmlMatchers.hasXPath("/metadata/groupId[text() = 'com.test']"),
                    XhtmlMatchers.hasXPath("/metadata/artifactId[text() = 'logger']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/latest[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/release[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '1.0']"),
                    XhtmlMatchers.hasXPath("metadata/versioning/versions[count(//version) = 1]"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/lastUpdated")
                )
            )
        );
    }

    @Test
    void addsMetadataChecksums() {
        new AstoMaven(this.storage).update(AstoMavenTest.UPLOAD, AstoMavenTest.PACKAGE)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            this.storage.list(AstoMavenTest.UPLOAD).join().stream()
                .map(key -> new KeyLastPart(key).get())
                .filter(key -> key.contains("maven-metadata.xml"))
                .toArray(String[]::new),
            Matchers.arrayContainingInAnyOrder(
                "maven-metadata.xml", "maven-metadata.xml.sha1", "maven-metadata.xml.sha256",
                "maven-metadata.xml.sha512", "maven-metadata.xml.md5"
            )
        );
    }

}
