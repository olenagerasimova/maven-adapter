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

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Head repository metadata.
 * @since 0.5
 */
final class RepoHead {

    /**
     * Client slice.
     */
    private final Slice client;

    /**
     * New repository artifact's heads.
     * @param client Client slice
     */
    RepoHead(final Slice client) {
        this.client = client;
    }

    /**
     * Artifact head.
     * @param path Path for artifact
     * @param headers Headers to send
     * @return Artifact headers
     */
    CompletionStage<Headers> head(final String path, final Headers headers) {
        final CompletableFuture<Headers> result = new CompletableFuture<>();
        this.client.response(
            new RequestLine(RqMethod.HEAD, path).toString(), headers, Flowable.empty()
        ).send(new HeadConnection(result));
        return result;
    }

    /**
     * Connection which accepts HEAD requests and returns headers to future.
     * @since 0.5
     */
    private static final class HeadConnection implements Connection {

        /**
         * Future to report the result.
         */
        private final CompletableFuture<Headers> future;

        /**
         * New head connection.
         * @param future Future for result
         */
        HeadConnection(final CompletableFuture<Headers> future) {
            this.future = future;
        }

        @Override
        public CompletionStage<Void> accept(final RsStatus status, final Headers headers,
            final Publisher<ByteBuffer> none) {
            if (status.success()) {
                this.future.complete(headers);
            } else {
                this.future.completeExceptionally(
                    new IllegalStateException(String.format("Unexpected status %s", status))
                );
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
