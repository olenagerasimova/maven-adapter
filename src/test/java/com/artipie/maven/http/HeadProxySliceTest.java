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
import com.artipie.asto.ext.PublisherAs;
import com.artipie.http.Headers;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rs.StandardRs;
import com.artipie.http.slice.SliceSimple;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link HeadProxySlice}.
 * @since 0.8
 */
class HeadProxySliceTest {

    @Test
    void performsRequestWithEmptyHeaderAndBody() {
        new HeadProxySlice(new SliceSimple(StandardRs.EMPTY)).response(
            "HEAD /some/path HTTP/1.1",
            new Headers.From("some", "value"),
            new Content.From("000".getBytes())
        ).send(
            (status, headers, body) -> {
                MatcherAssert.assertThat(
                    "Headers are empty",
                    headers,
                    new IsEmptyIterable<>()
                );
                MatcherAssert.assertThat(
                    "Body is empty",
                    new PublisherAs(body).bytes().toCompletableFuture().join(),
                    new IsEqual<>(new byte[]{})
                );
                return CompletableFuture.allOf();
            }
        );
    }

    @Test
    void passesStatusAndHeadersFromResponse() {
        final RsStatus status = RsStatus.CREATED;
        final Headers.From headers = new Headers.From("abc", "123");
        MatcherAssert.assertThat(
            new HeadProxySlice(
                new SliceSimple(new RsWithHeaders(new RsWithStatus(status), headers))
            ),
            new SliceHasResponse(
                Matchers.allOf(new RsHasStatus(status), new RsHasHeaders(headers)),
                new RequestLine(RqMethod.HEAD, "/")
            )
        );
    }

}