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
import com.artipie.asto.ext.PublisherAs;
import com.jcabi.xml.XMLDocument;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Read information from metadata file.
 * @since 0.5
 */
public final class ArtifactsMetadata {

    /**
     * Maven metadata xml name.
     */
    public static final String MAVEN_METADATA = "maven-metadata.xml";

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Ctor.
     * @param storage Storage
     */
    public ArtifactsMetadata(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Reads release version from maven-metadata.xml.
     * @param location Package location
     * @return Version as completed stage
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    public CompletionStage<String> release(final Key location) {
        return this.storage.value(new Key.From(location, ArtifactsMetadata.MAVEN_METADATA))
            .thenCompose(
                content -> new PublisherAs(content).string(StandardCharsets.UTF_8)
                .thenApply(
                    metadata -> {
                        final XMLDocument xml = new XMLDocument(metadata);
                        final String latest = "//latest/text()";
                        final String release = "//release/text()";
                        final String res;
                        if (!xml.xpath(release).isEmpty()) {
                            res = xml.xpath(release).get(0);
                        } else if (!xml.xpath(latest).isEmpty()) {
                            res = xml.xpath(latest).get(0);
                        } else {
                            throw new IllegalArgumentException(
                                "Maven metadata xml not valid: latest version not found"
                            );
                        }
                        return res;
                    }
                )
            );
    }

    /**
     * Reads group id and  artifact id from maven-metadata.xml.
     * @param location Package location
     * @return Pair of group id and artifact id
     */
    public CompletionStage<Pair<String, String>> groupAndArtifact(final Key location) {
        return this.storage.value(new Key.From(location, ArtifactsMetadata.MAVEN_METADATA))
            .thenCompose(
                content -> new PublisherAs(content).string(StandardCharsets.UTF_8)
                    .thenApply(XMLDocument::new)
                    .thenApply(
                        doc -> new ImmutablePair<>(
                            doc.xpath("//groupId/text()").get(0),
                            doc.xpath("//artifactId/text()").get(0)
                        )
                    )
            );
    }
}
