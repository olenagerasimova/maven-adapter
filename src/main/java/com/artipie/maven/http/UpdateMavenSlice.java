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
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.slice.KeyFromPath;
import com.artipie.maven.AstoMaven;
import com.artipie.maven.Maven;
import com.artipie.maven.repository.ValidUpload;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Upload maven artifact slice.
 * <p>
 * Starts metadata update after {@code maven-metadata.xml} upload.
 * </p>
 * @since 0.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class UpdateMavenSlice implements Slice {

    /**
     * Metadata pattern.
     */
    private static final Pattern PTN_META =
        Pattern.compile("^/(?<pkg>.+)/maven-metadata.xml$");

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Maven repo.
     */
    private final Maven maven;

    /**
     * Upload validation.
     */
    private final ValidUpload validator;

    /**
     * Ctor.
     * @param storage Storage
     * @param maven Maven repo
     * @param validator Upload validation
     */
    UpdateMavenSlice(final Storage storage, final Maven maven, final ValidUpload validator) {
        this.storage = storage;
        this.maven = maven;
        this.validator = validator;
    }

    /**
     * Ctor.
     * @param storage Storage
     */
    UpdateMavenSlice(final Storage storage) {
        this(storage, new AstoMaven(storage), new ValidUpload.Dummy());
    }

    @Override
    public Response response(final String line, final Iterable<Map.Entry<String, String>> head,
        final Publisher<ByteBuffer> body) {
        final RequestLineFrom reqline = new RequestLineFrom(line);
        final String path = reqline.uri().getPath();
        final Matcher matcher = PTN_META.matcher(path);
        return new AsyncResponse(
            this.storage.save(
                new KeyFromPath(new RequestLineFrom(line).uri().getPath()), new Content.From(body)
            ).thenCompose(
                ignored -> {
                    final CompletionStage<Response> res;
                    if (matcher.matches()) {
                        final Key location = new Key.From(matcher.group("pkg"));
                        res = this.validator.validate(location).thenCompose(
                            valid -> {
                                final CompletionStage<Response> upd;
                                if (valid) {
                                    upd = this.maven.update(location)
                                        .thenApply(nothing -> new RsWithStatus(RsStatus.CREATED));
                                } else {
                                    upd = this.storage.list(location).thenCompose(
                                        items -> CompletableFuture.allOf(
                                            items.stream().map(this.storage::delete)
                                                .toArray(CompletableFuture[]::new)
                                        )
                                    ).thenApply(
                                        nothing -> new RsWithStatus(RsStatus.BAD_REQUEST)
                                    );
                                }
                                return upd;
                            }
                        );
                    } else {
                        res = CompletableFuture.completedFuture(new RsWithStatus(RsStatus.CREATED));
                    }
                    return res;
                }
            )
        );
    }
}
