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
import com.artipie.asto.cache.StorageCache;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.client.jetty.JettyClientSlices;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.slice.LoggingSlice;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link MavenProxySlice} to verify it can work with central.
 * @since 0.6
 */
final class MavenProxySliceITCase {

    /**
     * Jetty client.
     */
    private final JettyClientSlices client = new JettyClientSlices();

    /**
     * Maven proxy.
     */
    private Slice proxy;

    /**
     * Test storage.
     */
    private Storage storage;

    @BeforeEach
    void setUp() throws Exception {
        this.client.start();
        this.storage = new InMemoryStorage();
        this.proxy = new LoggingSlice(
            new MavenProxySlice(
                this.client, URI.create("https://repo.maven.apache.org/maven2"),
                new StorageCache(this.storage)
            )
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        this.client.stop();
    }

    @Test
    void headRequestWorks() {
        MatcherAssert.assertThat(
            this.proxy.response(
                new RequestLine(RqMethod.HEAD, "/args4j/args4j/2.32/args4j-2.32.pom").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new RsHasStatus(RsStatus.OK)
        );
    }

    @Test
    void checksumRequestWorks() {
        MatcherAssert.assertThat(
            this.proxy.response(
                new RequestLine(RqMethod.GET, "/args4j/args4j/2.32/args4j-2.32.pom.md5")
                    .toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new RsHasStatus(RsStatus.OK)
        );
    }

    @Test
    void downloadsJarFromCentralAndCachesIt() {
        MatcherAssert.assertThat(
            "Response status is 200 OK",
            this.proxy.response(
                new RequestLine(RqMethod.GET, "/args4j/args4j/2.32/args4j-2.32.jar").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new RsHasStatus(RsStatus.OK)
        );
        MatcherAssert.assertThat(
            "Jar was saved to storage",
            this.storage.exists(new Key.From("args4j/args4j/2.32/args4j-2.32.jar")).join(),
            new IsEqual<>(true)
        );
    }

}