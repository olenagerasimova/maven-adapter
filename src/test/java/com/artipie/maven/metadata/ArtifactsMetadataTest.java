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

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.maven.MetadataXml;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ArtifactsMetadata}.
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ArtifactsMetadataTest {

    /**
     * Test storage.
     */
    private Storage storage;

    /**
     * Test key.
     */
    private Key key;

    @BeforeEach
    void initiate() {
        this.storage = new InMemoryStorage();
        this.key = new Key.From("com/test/logger");
    }

    @Test
    void readsMaxVersion() {
        final String expected = "1.10-SNAPSHOT";
        this.generate("0.3", expected, "1.0.1", "0.9-SNAPSHOT", "0.22");
        MatcherAssert.assertThat(
            new ArtifactsMetadata(this.storage).maxVersion(this.key).toCompletableFuture().join(),
            new IsEqual<>(expected)
        );
    }

    @Test
    void readsVersion() {
        final String expected = "1.0";
        this.generate(expected, "0.9");
        MatcherAssert.assertThat(
            new ArtifactsMetadata(this.storage).maxVersion(this.key).toCompletableFuture().join(),
            new IsEqual<>(expected)
        );
    }

    @Test
    void throwsExceptionOnInvalidMetadata() {
        this.generate();
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                CompletionException.class,
                () -> new ArtifactsMetadata(this.storage)
                    .maxVersion(this.key).toCompletableFuture().join()
            ).getCause(),
            new IsInstanceOf(IllegalArgumentException.class)
        );
    }

    @Test
    void readsGroupAndArtifactIds() {
        this.generate("8.0");
        MatcherAssert.assertThat(
            new ArtifactsMetadata(this.storage).groupAndArtifact(this.key)
                .toCompletableFuture().join(),
            new IsEqual<>(new ImmutablePair<>("com.test", "logger"))
        );
    }

    /**
     * Generates maven-metadata.xml.
     * @param versions Versions list
     */
    private void generate(final String... versions) {
        new MetadataXml("com.test", "logger").addXmlToStorage(
            this.storage, new Key.From(this.key, "maven-metadata.xml"),
            new MetadataXml.VersionTags(versions)
        );
    }

}
