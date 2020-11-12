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

import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.jcabi.log.Logger;
import io.reactivex.Flowable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
     * @return Artifact headers
     */
    CompletionStage<Optional<Headers>> head(final String path) {
        final CompletableFuture<Optional<Headers>> promise = new CompletableFuture<>();
        return this.client.response(
            new RequestLine(RqMethod.HEAD, path).toString(), Headers.EMPTY, Flowable.empty()
        ).send(
            (status, rsheaders, body) -> {
                final CompletionStage<Optional<Headers>> res;
                if (status == RsStatus.OK) {
                    res = CompletableFuture.completedFuture(Optional.of(rsheaders));
                } else {
                    res = CompletableFuture.completedFuture(Optional.empty());
                }
                return res.thenAccept(promise::complete).toCompletableFuture();
            }
        ).handle(
            (nothing, throwable) -> {
                if (throwable != null) {
                    Logger.error(this, throwable.getMessage());
                    promise.completeExceptionally(throwable);
                }
                return null;
            }
        ).thenCompose(nothing -> promise);
    }
}
