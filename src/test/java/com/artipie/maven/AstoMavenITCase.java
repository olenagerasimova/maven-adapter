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

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.maven.asto.AstoMaven;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.cactoos.list.ListOf;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link AstoMaven}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AstoMavenITCase {

    /**
     * Artifact key.
     */
    public static final Key.From ARTIFACT = new Key.From("com", "artipie", "asto");

    /**
     * Upload key.
     */
    public static final Key.From UPLOAD = new Key.From(".upload", "com", "artipie", "asto");

    /**
     * Temporary directory with repository data.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @TempDir
    Path repo;

    /**
     * Test repository.
     */
    private Storage repository;

    @BeforeEach
    void setUp() {
        final Storage resources = new FileStorage(new TestResource("com/artipie/asto").asPath());
        this.repository = new FileStorage(this.repo);
        final String latest = "0.20.2";
        final String meta = "maven-metadata.xml";
        resources.list(Key.ROOT).thenCompose(
            list -> CompletableFuture.allOf(list.stream()
                .filter(
                    item -> !item.string().contains("1.0-SNAPSHOT")
                        && !item.string().contains(latest)
                        && !item.string().contains(meta)
                )
                .map(
                    item -> resources.value(item).thenCompose(
                        content -> this.repository.save(
                            new Key.From(AstoMavenITCase.ARTIFACT, item), content
                        )
                    )
                )
                .toArray(CompletableFuture[]::new)
            )
        ).join();
        resources.list(Key.ROOT).thenCompose(
            list -> CompletableFuture.allOf(
                list.stream()
                .filter(
                    item -> item.string().contains(latest)
                    || item.string().contains(meta)
                )
                .map(
                    item -> resources.value(item).thenCompose(
                        content -> this.repository.save(
                            new Key.From(AstoMavenITCase.UPLOAD, item), content
                        )
                    )
                )
                .toArray(CompletableFuture[]::new)
            )
        ).join();
    }

    @Test
    void generatesMetadata() throws Exception {
        new AstoMaven(this.repository)
            .update(AstoMavenITCase.UPLOAD, AstoMavenITCase.ARTIFACT)
            .toCompletableFuture()
            .get();
        MatcherAssert.assertThat(
            new XMLDocument(
                this.repository.value(new Key.From(AstoMavenITCase.UPLOAD, "maven-metadata.xml"))
                .thenCompose(content -> new PublisherAs(content).string(StandardCharsets.UTF_8))
                    .join()
            ),
            new AllOf<>(
                new ListOf<Matcher<? super XML>>(
                    // @checkstyle LineLengthCheck (20 lines)
                    XhtmlMatchers.hasXPath("/metadata/groupId[text() = 'com.artipie']"),
                    XhtmlMatchers.hasXPath("/metadata/artifactId[text() = 'asto']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/latest[text() = '0.20.2']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/release[text() = '0.20.2']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.15']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.11.1']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.20.1']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.20.2']"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/versions/version[text() = '0.18']"),
                    XhtmlMatchers.hasXPath("metadata/versioning/versions[count(//version) = 5]"),
                    XhtmlMatchers.hasXPath("/metadata/versioning/lastUpdated")
                )
            )
        );
    }

}
