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

import com.artipie.maven.artifact.Artifact;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.iterable.Reversed;
import org.cactoos.list.ListOf;
import org.cactoos.text.PrefixOf;
import org.cactoos.text.Split;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.reactivestreams.Publisher;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Artifact metadata.
 *
 * Metadata is information about an artifact. It is a xml described in
 * http://maven.apache.org/ref/3.3.9/maven-repository-metadata/repository-metadata.html .
 *
 * @since 0.2
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (200 lines)
 */
public interface Metadata {

    /**
     * Artifact metadata content.
     *
     * @return Artifact metadata.
     */
    Publisher<ByteBuffer> content();

    /**
     * Maven metadata implementation.
     *
     * @since 0.2
     */
    class Maven implements Metadata {

        /**
         * Version tag name.
         */
        private static final String VERSION = "version";

        /**
         * ArtifactIf tag name.
         */
        private static final String ARTIFACTID = "artifactId";

        /**
         * Artifact for metadata retrieval.
         */
        private final Artifact artifact;

        /**
         * Constructor.
         * @param artifact Artifact for metadata retrieval.
         */
        public Maven(final Artifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public Publisher<ByteBuffer> content() {
            final List<String> versions = this.artifact.files().stream().map(
                file ->
                new UncheckedText(
                    new ListOf<>(
                        new Split(
                            new PrefixOf(
                                new UncheckedText(file.name()).asString(),
                                ".jar"
                            ),
                            new TextOf("-")
                        )
                    ).get(1)
                ).asString()
            ).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            final String latest = versions.get(0);
            return Flowable.just(
            ByteBuffer.wrap(
                new Xembler(
                    new Directives()
                    .add("metadata")
                    .attr("xmlns", "http://maven.apache.org/METADATA/1.1.0")
                    .attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
                    //@checkstyle LineLengthCheck (1 line)
                    .attr("xsi:schemaLocation", "http://maven.apache.org/METADATA/1.1.0 http://maven.apache.org/xsd/metadata-1.1.0.xsd")
                    .add("groupId").up()
                    .add(Maven.ARTIFACTID).up()
                    .add(Maven.VERSION).set(latest).up()
                    .add("versioning")
                    .add("latest").set(latest).up()
                    .add("release").set(latest).up()
                    .add("snapshot")
                    .add("timestamp").up()
                    .add("buildNumber").up()
                    .add("localCopy").up().up()
                    .add("versions")
                    .append(
                        () -> {
                            final Directives dirs = new Directives();
                            new Reversed<>(versions).forEach(
                                version -> dirs.add(Maven.VERSION).set(version).up()
                            );
                            return dirs.iterator();
                        }
                    ).up()
                    .add("lastUpdated").up()
                    .add("snapshotVersions")
                    .add("snapshotVersion")
                    .add("classifier").up()
                    .add("extension").up()
                    .add("value").up()
                    .add("updated").up().up().up().up()
                    .add("plugins")
                    .add("plugin")
                    .add("name").up()
                    .add("prefix").up()
                    .add(Maven.ARTIFACTID).up().up()
                )
                .xmlQuietly().replace("\r\n", "")
                    .getBytes()
            )
            );
        }
    }
}
