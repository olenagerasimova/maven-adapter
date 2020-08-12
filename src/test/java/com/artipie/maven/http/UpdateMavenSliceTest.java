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
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import com.artipie.maven.Maven;
import com.artipie.maven.ValidUpload;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link UpdateMavenSlice}.
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UpdateMavenSliceTest {

    @Test
    void uploadsFile() {
        final Storage storage = new InMemoryStorage();
        final byte[] body = "java code".getBytes();
        final String location = "org/example/artifact/0.1/artifact-1.0.jar";
        MatcherAssert.assertThat(
            "Returns CREATED status",
            new UpdateMavenSlice(storage).response(
                new RequestLine("PUT", String.format("/%s", location)).toString(),
                Collections.emptyList(),
                Flowable.fromArray(ByteBuffer.wrap(body))
            ),
            new RsHasStatus(RsStatus.CREATED)
        );
        MatcherAssert.assertThat(
            "Puts file to storage",
            new PublisherAs(storage.value(new Key.From(location)).join())
                .bytes().toCompletableFuture().join(),
            new IsEqual<>(body)
        );
    }

    @Test
    void removesArtifactIfUploadIsInvalid() {
        final Storage storage = new InMemoryStorage();
        final byte[] body = "java metadata".getBytes();
        final String location = "org/example/artifact/0.1/maven-metadata.xml";
        MatcherAssert.assertThat(
            "Returns BAD_REQUEST status",
            new UpdateMavenSlice(storage, new Maven.Fake(), new ValidUpload.Dummy(false)).response(
                new RequestLine("PUT", String.format("/%s", location)).toString(),
                Collections.emptyList(),
                Flowable.fromArray(ByteBuffer.wrap(body))
            ),
            new RsHasStatus(RsStatus.BAD_REQUEST)
        );
        MatcherAssert.assertThat(
            "Storage is empty",
            storage.list(Key.ROOT).join(),
            new IsEmptyIterable<>()
        );
    }

    @Test
    void updatesRepoIsMetadataIsValid() {
        final Storage storage = new InMemoryStorage();
        final byte[] body = "java metadata".getBytes();
        final String location = "org/example/artifact/0.1/maven-metadata.xml";
        final Maven.Fake maven = new Maven.Fake();
        MatcherAssert.assertThat(
            "Returns CREATED status",
            new UpdateMavenSlice(storage, maven, new ValidUpload.Dummy(true)).response(
                new RequestLine("PUT", String.format("/%s", location)).toString(),
                Collections.emptyList(),
                Flowable.fromArray(ByteBuffer.wrap(body))
            ),
            new RsHasStatus(RsStatus.CREATED)
        );
        MatcherAssert.assertThat(
            "Updates maven repo",
            maven.wasUpdated(),
            new IsEqual<>(true)
        );
    }

}
