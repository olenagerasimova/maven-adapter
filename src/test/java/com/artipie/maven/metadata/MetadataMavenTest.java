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

import com.artipie.maven.file.File;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.cactoos.Text;
import org.cactoos.io.BytesOf;
import org.cactoos.io.UncheckedBytes;
import org.cactoos.list.ListOf;
import org.cactoos.text.Joined;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

/**
 * Tests for {@link Metadata.Maven}.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (200 lines)
 */
public class MetadataMavenTest {

    @Test
    public void createsMetadata() throws Exception {
        MatcherAssert.assertThat(
            Flowable
                .fromPublisher(
                    new Metadata.Maven(
                        () -> new ListOf<>(
                            new FakeFile(
                                new TextOf("artifact-0.1.jar"),
                                "jar file 0.1".getBytes(StandardCharsets.UTF_8)
                            ),
                            new FakeFile(
                                new TextOf("artifact-0.1-source.jar"),
                                "source file 0.1".getBytes(StandardCharsets.UTF_8)
                            ),
                            new FakeFile(
                                new TextOf("artifact-0.1-javadoc.jar"),
                                "javadoc file 0.1".getBytes(StandardCharsets.UTF_8)
                            ),
                            new FakeFile(
                                new TextOf("artifact-0.2.jar"),
                                new byte[0]
                            ),
                            new FakeFile(
                                new TextOf("artifact-0.2-source.jar"),
                                new byte[0]
                            ),
                            new FakeFile(
                                new TextOf("artifact-0.2-javadoc.jar"),
                                new byte[0]
                            ),
                            new FakeFile(
                                new TextOf("artifact-1.0.jar"),
                                new byte[0]
                            ),
                            new FakeFile(
                                new TextOf("artifact-1.0-source.jar"),
                                new byte[0]
                            ),
                            new FakeFile(
                                new TextOf("artifact-1.0-javadoc.jar"),
                                new byte[0]
                            )
                        )
                    ).content()
                ).concatMap(
                    buffer -> Flowable.just(buffer.array())
                ).reduce(
                    (arr1, arr2) ->
                        ByteBuffer.wrap(
                            new byte[arr1.length + arr2.length]
                        ).put(arr1).put(arr2).array()
                ).blockingGet(),
            new IsEqual<>(
                new UncheckedBytes(
                    new BytesOf(
                        new Joined(
                            "",
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
                            "<metadata xmlns=\"http://maven.apache.org/METADATA/1.1.0\" ",
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ",
                            "xsi:schemaLocation=\"http://maven.apache.org/METADATA/1.1.0 ",
                            "http://maven.apache.org/xsd/metadata-1.1.0.xsd\">",
                            "<groupId/><artifactId/><version>1.0</version><versioning>",
                            "<latest>1.0</latest><release>1.0</release><snapshot>",
                            "<timestamp/><buildNumber/><localCopy/></snapshot>",
                            "<versions><version>0.1</version><version>0.2</version>",
                            "<version>1.0</version></versions><lastUpdated/>",
                            "<snapshotVersions><snapshotVersion><classifier/>",
                            "<extension/><value/><updated/></snapshotVersion>",
                            "</snapshotVersions></versioning><plugins><plugin>",
                            "<name/><prefix/><artifactId/></plugin></plugins></metadata>"
                        )
                    )
                ).asBytes()
            )
        );
    }

    /**
     * Fake file to be used in tests.
     *
     * @since 0.2
     */
    private class FakeFile implements File {

        /**
         * File content.
         */
        private final ByteBuffer bytecontent;

        /**
         * File name.
         */
        private final Text filename;

        /**
         * Contructor.
         *
         * @param name File name.
         * @param content File content.
         */
        FakeFile(final Text name, final byte[] content) {
            this.filename = name;
            this.bytecontent = ByteBuffer.wrap(new UncheckedBytes(new BytesOf(content)).asBytes());
        }

        @Override
        public Publisher<ByteBuffer> content() {
            return Flowable.just(this.bytecontent);
        }

        @Override
        public Text name() {
            return this.filename;
        }
    }
}
