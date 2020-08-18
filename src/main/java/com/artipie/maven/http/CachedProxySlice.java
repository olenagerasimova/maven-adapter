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
import com.artipie.asto.cache.Cache;
import com.artipie.asto.cache.CacheControl;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.slice.KeyFromPath;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Maven proxy slice with cache support.
 * @since 0.5
 */
final class CachedProxySlice implements Slice {

    /**
     * Origin slice.
     */
    private final Slice origin;

    /**
     * Cache.
     */
    private final Cache cache;

    /**
     * Wraps origin slice with caching layer.
     * @param origin Origin slice
     * @param cache Cache
     */
    CachedProxySlice(final Slice origin, final Cache cache) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public Response response(final String line, final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final Key key = new KeyFromPath(new RequestLineFrom(line).uri().getPath());
        return new AsyncResponse(
            this.cache.load(
                key,
                () -> CompletableFuture.completedFuture(
                    new Content.From(
                        new ProxyPublisher(this.origin.response(line, headers, body))
                    )
                ),
                CacheControl.Standard.ALWAYS
            ).thenApply(RsWithBody::new)
        );
    }

    /**
     * Proxy publisher from response.
     * @since 0.5
     */
    private static final class ProxyPublisher implements Publisher<ByteBuffer> {

        /**
         * Slice response.
         */
        private final Response response;

        /**
         * Ctor.
         * @param response Slice response
         */
        ProxyPublisher(final Response response) {
            this.response = response;
        }

        @Override
        public void subscribe(final Subscriber<? super ByteBuffer> subscriber) {
            this.response.send(
                (status, headers, body) -> {
                    final CompletableFuture<Void> future = new CompletableFuture<>();
                    body.subscribe(new CompletableSubscriber(subscriber, future));
                    return future;
                }
            );
        }
    }

    /**
     * Subscriber decorator that completes {@link CompletableFuture} on error and on complete.
     * @since 0.5
     */
    private static final class CompletableSubscriber implements Subscriber<ByteBuffer> {

        /**
         * Subscriber.
         */
        private final Subscriber<? super ByteBuffer> origin;

        /**
         * Completable future.
         */
        private final CompletableFuture<Void> future;

        /**
         * Ctor.
         * @param origin Subscriber
         * @param future Completable
         */
        CompletableSubscriber(final Subscriber<? super ByteBuffer> origin,
            final CompletableFuture<Void> future) {
            this.origin = origin;
            this.future = future;
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            this.origin.onSubscribe(new CompletableSubscription(subscription, this.future));
        }

        @Override
        public void onNext(final ByteBuffer buffer) {
            this.origin.onNext(buffer);
        }

        @Override
        public void onError(final Throwable err) {
            try {
                this.origin.onError(err);
            } finally {
                this.future.completeExceptionally(err);
            }
        }

        @Override
        public void onComplete() {
            try {
                this.origin.onComplete();
            } finally {
                this.future.complete(null);
            }
        }
    }

    /**
     * Subscription decorator that cancels {@link CompletableFuture} on subscription cancellation.
     * @since 0.5
     */
    private static final class CompletableSubscription implements Subscription {

        /**
         * Origin subscription.
         */
        private final Subscription origin;

        /**
         * Completable future.
         */
        private final CompletableFuture<Void> future;

        /**
         * Ctor.
         * @param origin Subscription
         * @param future Completable
         */
        CompletableSubscription(final Subscription origin, final CompletableFuture<Void> future) {
            this.origin = origin;
            this.future = future;
        }

        @Override
        public void request(final long num) {
            this.origin.request(num);
        }

        @Override
        public void cancel() {
            try {
                this.origin.cancel();
            } finally {
                this.future.cancel(true);
            }
        }
    }
}
