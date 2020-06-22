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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.rx.RxStorageWrapper;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * Proxy cache with based on {@link Storage}.
 * @since 0.5
 * @todo #98:30min Configure proxy cache with parameters:
 *  - TTL (time to live) for caching objects
 *  - Max size to configure storage size for caching
 */
public final class StorageCache implements ProxyCache {

    /**
     * Cache storage.
     */
    private final Storage storage;

    /**
     * New cache.
     * @param storage Cache storage
     */
    public StorageCache(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public CompletionStage<? extends Content> load(
        final Key key, final Supplier<? extends CompletionStage<? extends Content>> remote
    ) {
        final RxStorageWrapper rxsto = new RxStorageWrapper(this.storage);
        return rxsto.exists(key).filter(exists -> exists)
            .flatMapSingleElement(ignore -> rxsto.value(key))
            .switchIfEmpty(
                SingleInterop.fromFuture(remote.get()).map(
                    content -> {
                        final PublishSubject<ByteBuffer> subj = PublishSubject.create();
                        rxsto.save(
                            key,
                            new Content.From(
                                content.size(), subj.toFlowable(BackpressureStrategy.ERROR)
                            )
                        ).subscribe();
                        final Flowable<ByteBuffer> flow = Flowable.fromPublisher(content);
                        return new Content.From(
                            content.size(),
                            flow.doOnNext(subj::onNext).doOnError(subj::onError)
                                .doOnComplete(subj::onComplete)
                        );
                    }
                )
            ).to(SingleInterop.get());
    }
}
