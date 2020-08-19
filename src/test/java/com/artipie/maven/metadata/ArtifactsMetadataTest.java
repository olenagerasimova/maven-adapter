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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
     * @todo #144:30min Extract this method into class in test scope: create class to generate and
     *  add maven-metadata.xml to storage, use this new class here and in AstoMavenITCase.
     */
    private void generate(final String... versions) {
        this.storage.save(
            new Key.From(this.key, "maven-metadata.xml"),
            new Content.From(
                String.join(
                    "\n",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<metadata>",
                    "  <groupId>com.test</groupId>",
                    "  <artifactId>logger</artifactId>",
                    "  <versioning>",
                    "    <versions>",
                    Stream.of(versions)
                        .map(version -> String.format("      <version>%s</version>", version))
                        .collect(Collectors.joining("\n")),
                    "    </versions>",
                    "    <lastUpdated>20200804141716</lastUpdated>",
                    "  </versioning>",
                    "</metadata>"
                ).getBytes()
            )
        ).join();
    }

}
