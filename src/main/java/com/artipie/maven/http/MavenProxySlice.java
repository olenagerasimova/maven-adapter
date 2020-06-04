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
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.jcabi.log.Logger;
import hu.akarnokd.rxjava2.interop.CompletableInterop;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.reactive.client.ReactiveRequest;
import org.reactivestreams.Publisher;

/**
 * Maven proxy repository slice.
 * @since 0.5
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class MavenProxySlice implements Slice {

    /**
     * Target URI.
     */
    private final URI uri;

    /**
     * HTTP client.
     */
    private final HttpClient http;

    /**
     * Proxy for URI.
     * @param uri URI
     */
    public MavenProxySlice(final URI uri) {
        this.uri = uri;
        this.http = new HttpClient();
    }

    @Override
    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.OnlyOneReturn"})
    public Response response(final String line, final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        if (this.http.isRunning()) {
            try {
                this.http.start();
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception err) {
                Logger.error(this, "Failed to start jetty client: %[exception]s", err);
                return new RsWithStatus(RsStatus.INTERNAL_ERROR);
            }
        }
        final URIBuilder builder = new URIBuilder(this.uri);
        final String path = new RequestLineFrom(line).uri().getPath();
        builder.setPath(Paths.get(builder.getPath(), path).normalize().toString());
        return connection -> {
            final Request request = this.http.newRequest(builder.toString());
            return Flowable.fromPublisher(
                ReactiveRequest.newBuilder(request).build().response(
                    (rsp, content) -> {
                        final Completable res;
                        if (rsp.getStatus() == HttpURLConnection.HTTP_OK) {
                            res = Completable.defer(
                                () -> CompletableInterop.fromFuture(
                                    connection.accept(
                                        RsStatus.OK, Headers.EMPTY,
                                        Flowable.fromPublisher(content).map(chunk -> chunk.buffer)
                                    )
                                )
                            );
                        } else {
                            res = Completable.defer(
                                () -> CompletableInterop.fromFuture(
                                    connection.accept(
                                        RsStatus.INTERNAL_ERROR, Headers.EMPTY,
                                        Flowable.just(
                                            ByteBuffer.wrap(
                                                rsp.toString().getBytes(StandardCharsets.UTF_8)
                                            )
                                        )
                                    )
                                )
                            );
                        }
                        return res.toFlowable();
                    }
                )
            ).ignoreElements().to(CompletableInterop.await());
        };
    }
}
