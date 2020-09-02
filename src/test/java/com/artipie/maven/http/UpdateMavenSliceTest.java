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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Headers;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.maven.Maven;
import com.artipie.maven.ValidUpload;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link UpdateMavenSlice}.
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle IllegalCatchCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.AvoidCatchingGenericException"})
class UpdateMavenSliceTest {

    @Test
    void uploadsFileToTempLocation() {
        final Storage storage = new InMemoryStorage();
        final byte[] body = "java code".getBytes();
        final String location = "org/example/artifact/0.1/artifact-1.0.jar";
        MatcherAssert.assertThat(
            "Returns CREATED status",
            new UpdateMavenSlice(storage),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine("PUT", String.format("/%s", location)),
                Headers.EMPTY, new Content.From(body)
            )
        );
        MatcherAssert.assertThat(
            "Puts file to storage",
            new PublisherAs(storage.value(new Key.From(UpdateMavenSlice.TEMP, location)).join())
                .bytes().toCompletableFuture().join(),
            new IsEqual<>(body)
        );
    }

    @Test
    void movesFilesToMainStorageAfterUpdate() {
        final Storage storage = new InMemoryStorage();
        final byte[] jar = "java code".getBytes();
        final String jlocation = "org/example/artifact/0.1/artifact-1.0.jar";
        final byte[] meta = "java metadata".getBytes();
        final String mlocation = "org/example/artifact/0.1/maven-metadata.xml";
        final UpdateMavenSlice update = new UpdateMavenSlice(
            storage, new Maven.Fake(), new ValidUpload.Dummy()
        );
        MatcherAssert.assertThat(
            "Returns CREATED status for jar",
            update,
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine("PUT", String.format("/%s", jlocation)),
                Headers.EMPTY, new Content.From(jar)
            )
        );
        MatcherAssert.assertThat(
            "Returns CREATED status for metadata",
            update,
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine("PUT", String.format("/%s", mlocation)),
                Headers.EMPTY, new Content.From(meta)
            )
        );
        MatcherAssert.assertThat(
            "Puts jar to main storage",
            new PublisherAs(storage.value(new Key.From(jlocation)).join())
                .bytes().toCompletableFuture().join(),
            new IsEqual<>(jar)
        );
        MatcherAssert.assertThat(
            "Puts meta to main storage",
            new PublisherAs(storage.value(new Key.From(mlocation)).join())
                .bytes().toCompletableFuture().join(),
            new IsEqual<>(meta)
        );
        MatcherAssert.assertThat(
            "Removes files from temp location",
            storage.list(UpdateMavenSlice.TEMP).join(),
            new IsEmptyCollection<>()
        );
    }

    @Test
    void removesArtifactIfUploadIsInvalid() {
        final Storage storage = new InMemoryStorage();
        final byte[] body = "java metadata".getBytes();
        final String location = "org/example/artifact/0.1/maven-metadata.xml";
        MatcherAssert.assertThat(
            "Returns BAD_REQUEST status",
            new UpdateMavenSlice(storage, new Maven.Fake(), new ValidUpload.Dummy(false)),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.BAD_REQUEST),
                new RequestLine("PUT", String.format("/%s", location)),
                Headers.EMPTY, new Content.From(body)
            )
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
            new UpdateMavenSlice(storage, maven, new ValidUpload.Dummy(true)),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine("PUT", String.format("/%s", location)),
                Headers.EMPTY, new Content.From(body)
            )
        );
        MatcherAssert.assertThat(
            "Updates maven repo",
            maven.wasUpdated(),
            new IsEqual<>(true)
        );
    }

    @Test
    void doesNotStartUpdateWhenSnapshotMetadataIsSent() {
        final Storage storage = new InMemoryStorage();
        final byte[] meta = "snapshot metadata".getBytes();
        final String mlocation = "org/example/artifact/0.2-SNAPSHOT/maven-metadata.xml";
        final Maven.Fake maven = new Maven.Fake();
        MatcherAssert.assertThat(
            "Returns CREATED status for snapshot metadata",
            new UpdateMavenSlice(storage, maven, new ValidUpload.Dummy()),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.CREATED),
                new RequestLine("PUT", String.format("/%s", mlocation)),
                Headers.EMPTY, new Content.From(meta)
            )
        );
        MatcherAssert.assertThat(
            "Does not start update",
            maven.wasUpdated(),
            new IsEqual<>(false)
        );
        MatcherAssert.assertThat(
            "Puts snapshot metadata to storage",
            new PublisherAs(storage.value(new Key.From(UpdateMavenSlice.TEMP, mlocation)).join())
                .bytes().toCompletableFuture().join(),
            new IsEqual<>(meta)
        );
    }

    @RepeatedTest(10)
    void throwsExceptionWhenMetadataUpdatesDoneSimultaneously() {
        final Storage storage = new InMemoryStorage();
        final int count = 3;
        final CountDownLatch latch = new CountDownLatch(count);
        final List<CompletableFuture<Void>> tasks = new ArrayList<>(count);
        for (int number = 0; number < count; number += 1) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            tasks.add(future);
            new Thread(
                () -> {
                    try {
                        latch.countDown();
                        latch.await();
                        new UpdateMavenSlice(
                            storage,
                            new Maven.Fake(),
                            new ValidUpload.Dummy(true)
                        ).response(
                            new RequestLine(
                                RqMethod.PUT,
                                String.format("/%s", "org/example/artifact/0.1/maven-metadata.xml")
                            ).toString(),
                            Headers.EMPTY,
                            new Content.From("java metadata".getBytes())
                        ).send(
                            (status, headers, body) -> CompletableFuture.allOf()
                        ).toCompletableFuture().join();
                        future.complete(null);
                    } catch (final Exception exception) {
                        future.completeExceptionally(exception);
                    }
                }
            ).start();
        }
        for (final CompletableFuture<Void> task : tasks) {
            try {
                task.join();
            } catch (final Exception ignored) {
            }
        }
        MatcherAssert.assertThat(
            "Some updates failed",
            tasks.stream().anyMatch(CompletableFuture::isCompletedExceptionally),
            new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
            "Storage has no locks",
            storage.list(Key.ROOT).join().stream()
                .noneMatch(key -> key.string().contains("lock")),
            new IsEqual<>(true)
        );
    }
}
