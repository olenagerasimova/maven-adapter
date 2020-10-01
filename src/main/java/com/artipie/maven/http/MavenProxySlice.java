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

import com.artipie.asto.cache.Cache;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.client.ClientSlices;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import com.artipie.http.slice.SliceSimple;
import io.reactivex.Flowable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.client.utils.URIBuilder;
import org.reactivestreams.Publisher;

/**
 * Maven proxy repository slice.
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class MavenProxySlice extends Slice.Wrap {

    /**
     * New maven proxy without cache.
     * @param clients HTTP clients
     * @param remote Remote URI
     */
    public MavenProxySlice(final ClientSlices clients, final URI remote) {
        this(clients, remote, Cache.NOP);
    }

    /**
     * New Maven proxy slice with cache.
     * @param clients HTTP clients
     * @param remote Remote URI
     * @param cache Repository cache
     */
    public MavenProxySlice(final ClientSlices clients, final URI remote, final Cache cache) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new ByMethodsRule(RqMethod.HEAD),
                    new HeadProxySlice(new ClientSlice(clients, remote))
                ),
                new RtRulePath(
                    new ByMethodsRule(RqMethod.GET),
                    new CachedProxySlice(new ClientSlice(clients, remote), cache)
                ),
                new RtRulePath(
                    RtRule.FALLBACK,
                    new SliceSimple(new RsWithStatus(RsStatus.METHOD_NOT_ALLOWED))
                )
            )
        );
    }

    /**
     * Client slice.
     * @since 0.5
     * @todo #128:30min This class will be moved to `artipie/http-client` repository.
     *  Then update http-client dependency version, use http-client's version
     *  and remove this class here.
     */
    private static final class ClientSlice implements Slice {

        /**
         * Client HTTP slices.
         */
        private final ClientSlices clients;

        /**
         * Remote URI.
         */
        private final URI remote;

        /**
         * New client slice from remote URI.
         * @param clients Slice clients
         * @param remote Remote URI
         */
        ClientSlice(final ClientSlices clients, final URI remote) {
            this.clients = clients;
            this.remote = remote;
        }

        @Override
        public Response response(final String line,
            final Iterable<Map.Entry<String, String>> headers, final Publisher<ByteBuffer> body) {
            final Slice slice;
            final String host = this.remote.getHost();
            final int port = this.remote.getPort();
            final String scheme = this.remote.getScheme();
            switch (scheme) {
                case "https":
                    slice = this.clients.https(host, port);
                    break;
                case "http":
                    slice = this.clients.http(host, port);
                    break;
                default:
                    throw new IllegalStateException(
                        String.format("Scheme '%s' is not supported", scheme)
                    );
            }
            final RequestLineFrom rqline = new RequestLineFrom(line);
            final URI uri = rqline.uri();
            final CompletableFuture<Response> promise = new CompletableFuture<>();
            slice.response(
                new RequestLine(
                    rqline.method().value(),
                    new URIBuilder(uri)
                        .setPath(concatPaths(this.remote.getPath(), uri.getPath()))
                        .toString(),
                    rqline.version()
                ).toString(),
                headers, body
            ).send(
                (status, rsheaders, rsbody) -> {
                    final CompletableFuture<Void> terminated = new CompletableFuture<>();
                    final Flowable<ByteBuffer> termbody = Flowable.fromPublisher(rsbody)
                        .doOnError(terminated::completeExceptionally)
                        .doOnTerminate(() -> terminated.complete(null));
                    promise.complete(new RsFull(status, rsheaders, termbody));
                    return terminated;
                }
            );
            return new AsyncResponse(promise);
        }

        /**
         * Concat multiple paths into single.
         * @param paths URI paths
         * @return Merged path string
         */
        private static String concatPaths(final String... paths) {
            final String rel = Stream.of(paths).map(
                path -> path.replaceAll("(?:^/|/$)", "")
            ).flatMap(path -> Arrays.stream(path.split("/")))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining("/"));
            return String.format("/%s", rel);
        }
    }
}
