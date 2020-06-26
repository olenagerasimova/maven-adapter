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
package com.artipie.maven.repository;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Remaining;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.memory.InMemoryStorage;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.reactivestreams.Subscriber;

/**
 * Test case for {@link StorageCache}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class StorageCacheTest {

    @Test
    void loadsFromRemote(@TempDir final Path tmp) throws Exception {
        final Storage storage = new FileStorage(tmp);
        final Key remote = new Key.From("remote");
        final Key cached = new Key.From("cached");
        final byte[] data = new byte[]{0x0a, 0x0b, 0x0c, 0x0d};
        new StorageCache(storage).load(
            cached,
            () -> CompletableFuture.supplyAsync(
                () -> new Content.From(new TestPublisher(data))
            )
        ).thenCompose(content -> storage.save(remote, content)).toCompletableFuture().get();
        MatcherAssert.assertThat(
            "Loaded content is differ from origin",
            new BlockingStorage(storage).value(remote),
            Matchers.equalTo(data)
        );
        MatcherAssert.assertThat(
            "Cache didn't save data to storage",
            new BlockingStorage(storage).value(cached),
            Matchers.equalTo(data)
        );
    }

    @Test
    @Disabled
    void doesntSaveFailedPublisher() throws Exception {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("failed");
        MatcherAssert.assertThat(
            "Cached didn't throw an exception",
            Assertions.assertThrows(
                ExecutionException.class,
                () -> new Concatenation(
                    new StorageCache(storage).load(
                        key,
                        () -> CompletableFuture.supplyAsync(
                            () -> new Content.From(
                                Flowable.error(new IllegalStateException("Error"))
                            )
                        )
                    ).toCompletableFuture().get()
                ).single().blockingGet()
            ),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            "Cache saves data to storage on error",
            new BlockingStorage(storage).exists(key),
            Matchers.is(false)
        );
    }

    @Test
    void loadsFromCache(@TempDir final Path tmp) throws Exception {
        final Storage storage = new FileStorage(tmp);
        final Key key = new Key.From("cache");
        final byte[] data = new byte[]{0x00, 0x01, 0x02, 0x03};
        new BlockingStorage(storage).save(key, data);
        MatcherAssert.assertThat(
            SingleInterop.fromFuture(
                new StorageCache(storage).load(
                    key,
                    () -> Single.<Content>error(new IllegalStateException("Remote error"))
                        .to(SingleInterop.get())
                )
            ).flatMap(content -> new Concatenation(content).single())
                .map(buf -> new Remaining(buf).bytes())
                .blockingGet(),
            Matchers.equalTo(data)
        );
    }

    /**
     * Lazy publisher generator.
     * @since 0.5
     */
    private static final class TestPublisher extends Flowable<ByteBuffer> {

        /**
         * Data to return.
         */
        private final byte[] data;

        /**
         * New test publisher.
         * @param data Data to publish
         */
        TestPublisher(final byte[] data) {
            this.data = Arrays.copyOf(data, data.length);
        }

        @Override
        public void subscribeActual(final Subscriber<? super ByteBuffer> sub) {
            final AtomicInteger pos = new AtomicInteger();
            Flowable.<Byte>generate(
                emitter -> {
                    final int next = pos.getAndIncrement();
                    if (next < this.data.length) {
                        emitter.onNext(this.data[next]);
                    } else {
                        emitter.onComplete();
                    }
                }
            ).map(bte -> ByteBuffer.wrap(new byte[]{bte})).subscribe(sub);
        }
    }
}
