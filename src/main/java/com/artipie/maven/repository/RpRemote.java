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

import com.artipie.http.Headers;
import com.artipie.http.Response;
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
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.reactive.client.ReactiveRequest;

/**
 * {@link Repository} getting artifacts from a remote {@link URI}.
 *
 * @since 0.5
 * @todo #92:30min Introduce constructor parameters to configure the
 *  auth credentials necessary to contact the remote URI. See #92 for details
 *  of the required information.
 * @todo #92:30min Add support for caching the downloaded artifacts locally. See #92
 *  for the required parameters that should be passed to the constructor of this cache.
 *  A good idea would be to implement this cache as a decorator of the Repository interface.
 * @checkstyle ReturnCountCheck (500 lines)
 */
public final class RpRemote implements Repository {

    /**
     * Target URI.
     */
    private final URI remote;

    /**
     * HTTP client.
     */
    private final HttpClient http;

    /**
     * Proxy for URI.
     * @param uri URI
     */
    public RpRemote(final URI uri) {
        this.remote = uri;
        this.http = new HttpClient();
    }

    @Override
    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.OnlyOneReturn"})
    public Response response(final URI uri) {
        if (!this.http.isRunning()) {
            try {
                this.http.start();
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception err) {
                Logger.error(this, "Failed to start jetty client: %[exception]s", err);
                return new RsWithStatus(RsStatus.INTERNAL_ERROR);
            }
        }
        final URIBuilder builder = new URIBuilder(this.remote);
        builder.setPath(Paths.get(builder.getPath(), uri.getPath()).normalize().toString());
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
