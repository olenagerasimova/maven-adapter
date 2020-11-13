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
import com.artipie.asto.FailedCompletionStage;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.slice.SliceSimple;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CachedProxySlice}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class CachedProxySliceTest {

    @Test
    void loadsCachedContent() {
        final byte[] data = "cache".getBytes();
        MatcherAssert.assertThat(
            new CachedProxySlice(
                (line, headers, body) -> new RsWithBody(ByteBuffer.wrap("123".getBytes())),
                (key, supplier, control) -> CompletableFuture.supplyAsync(
                    () -> Optional.of(new Content.From(data))
                )
            ),
            new SliceHasResponse(
                Matchers.allOf(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(data)
                ),
                new RequestLine(RqMethod.GET, "/foo")
            )
        );
    }

    @Test
    @Disabled
    void returnsNotFoundOnRemoteError() {
        MatcherAssert.assertThat(
            new CachedProxySlice(
                new SliceSimple(new RsWithStatus(RsStatus.INTERNAL_ERROR)),
                (key, supplier, control) -> supplier.get()
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.NOT_FOUND),
                new RequestLine(RqMethod.GET, "/any")
            )
        );
    }

    @Test
    void returnsNotFoundOnRemoteAndCacheError() {
        MatcherAssert.assertThat(
            new CachedProxySlice(
                new SliceSimple(new RsWithStatus(RsStatus.INTERNAL_ERROR)),
                (key, supplier, control)
                    -> new FailedCompletionStage<>(new RuntimeException("Any error"))
            ),
            new SliceHasResponse(
                new RsHasStatus(RsStatus.NOT_FOUND),
                new RequestLine(RqMethod.GET, "/abc")
            )
        );
    }

    @Test
    void loadsOriginIfCacheNotFound() {
        final byte[] data = "remote".getBytes();
        MatcherAssert.assertThat(
            new CachedProxySlice(
                (line, headers, body) -> new RsWithBody(ByteBuffer.wrap(data)),
                (key, supplier, control) -> supplier.get()
            ),
            new SliceHasResponse(
                Matchers.allOf(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasBody(data)
                ),
                new RequestLine(RqMethod.GET, "/bar")
            )
        );
    }
}
