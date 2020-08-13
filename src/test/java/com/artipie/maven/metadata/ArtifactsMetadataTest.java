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
import com.artipie.asto.test.TestResource;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ArtifactsMetadata}.
 * @since 0.5
 */
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
        new TestResource("maven-metadata.xml.example")
            .saveTo(this.storage, new Key.From(this.key, "maven-metadata.xml"));
    }

    @Test
    void readsVersion() {
        MatcherAssert.assertThat(
            new ArtifactsMetadata(this.storage).latest(this.key).toCompletableFuture().join(),
            new IsEqual<>("1.0")
        );
    }

    @Test
    void readsGroupAndArtifactIds() {
        MatcherAssert.assertThat(
            new ArtifactsMetadata(this.storage).groupAndArtifact(this.key)
                .toCompletableFuture().join(),
            new IsEqual<>(new ImmutablePair<>("com.test", "logger"))
        );
    }

}
