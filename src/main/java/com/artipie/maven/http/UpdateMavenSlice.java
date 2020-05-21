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
import com.artipie.http.Connection;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.slice.SliceUpload;
import com.artipie.maven.Maven;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Upload maven artifact slice.
 * <p>
 * Starts metadata update after {@code maven-metadata.xml} upload.
 * </p>
 * @since 0.4
 */
final class UpdateMavenSlice implements Slice {

    /**
     * Metadata pattern.
     */
    private static final Pattern PTN_META =
        Pattern.compile("^/(?<pkg>.+)/maven-metadata.xml$");

    /**
     * Origin upload slice.
     */
    private final Slice origin;

    /**
     * Maven repo.
     */
    private final Maven maven;

    /**
     * Ctor.
     * @param storage Storage
     */
    UpdateMavenSlice(final Storage storage) {
        this.origin = new SliceUpload(storage);
        this.maven = new Maven(storage);
    }

    @Override
    public Response response(final String line, final Iterable<Map.Entry<String, String>> head,
        final Publisher<ByteBuffer> body) {
        final RequestLineFrom reqline = new RequestLineFrom(line);
        final String path = reqline.uri().getPath();
        final Matcher matcher = PTN_META.matcher(path);
        return new ResponseWrap(
            this.origin.response(line, head, body),
            () -> {
                final CompletionStage<Void> res;
                if (matcher.matches()) {
                    res = this.maven.update(new Key.From(matcher.group("pkg")));
                } else {
                    res = CompletableFuture.completedFuture(null);
                }
                return res;
            }
        );
    }

    /**
     * Response decorator which starts task after response.
     * @since 0.4
     */
    private static final class ResponseWrap implements Response {

        /**
         * Origin response.
         */
        private final Response origin;

        /**
         * Update task.
         */
        private final Supplier<CompletionStage<Void>> update;

        /**
         * Ctor.
         * @param response Origin response
         * @param update Update task
         */
        ResponseWrap(final Response response, final Supplier<CompletionStage<Void>> update) {
            this.origin = response;
            this.update = update;
        }

        @Override
        public CompletionStage<Void> send(final Connection connection) {
            return this.origin.send(connection).thenCompose(none -> this.update.get());
        }
    }
}
