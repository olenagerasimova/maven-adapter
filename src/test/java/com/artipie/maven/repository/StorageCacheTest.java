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
import com.artipie.asto.memory.InMemoryStorage;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StorageCache}.
 *
 * @since 0.5
 */
final class StorageCacheTest {

    @Test
    void loadsFromRemote() throws Exception {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("item");
        final byte[] data = new byte[]{0x0a, 0x0b, 0x0c, 0x0d};
        MatcherAssert.assertThat(
            "Loaded content is differ from origin",
            new Concatenation(
                new StorageCache(storage)
                    .load(key, () -> CompletableFuture.supplyAsync(() -> new Content.From(data)))
                    .toCompletableFuture()
                    .get()
            ).single()
                .map(buf -> new Remaining(buf).bytes()).blockingGet(),
            Matchers.equalTo(data)
        );
        MatcherAssert.assertThat(
            "Cache didn't save data to storage",
            new BlockingStorage(storage).value(key),
            Matchers.equalTo(data)
        );
    }

    @Test
    void loadsFromCache() throws Exception {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("cached");
        final byte[] data = new byte[]{0x00, 0x01, 0x02, 0x03};
        new BlockingStorage(storage).save(key, data);
        MatcherAssert.assertThat(
            SingleInterop.fromFuture(
                new StorageCache(storage).load(
                    key,
                    () -> Single.<Content>error(new IllegalStateException("Error"))
                        .to(SingleInterop.get())
                )
            ).flatMap(content -> new Concatenation(content).single())
                .map(buf -> new Remaining(buf).bytes())
                .blockingGet(),
            Matchers.equalTo(data)
        );
    }
}
