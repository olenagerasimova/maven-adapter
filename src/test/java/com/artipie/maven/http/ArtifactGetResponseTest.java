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
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rs.RsStatus;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ArtifactGetResponse}.
 *
 * @since 0.5
 * @checkstyle JavadocMethodCheck (500 lines)
 */
final class ArtifactGetResponseTest {

    @Test
    void okIfArtifactExists() throws Exception {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("repo/artifact.jar");
        new BlockingStorage(storage).save(key, "something".getBytes());
        MatcherAssert.assertThat(
            new ArtifactGetResponse(storage, key),
            new RsHasStatus(RsStatus.OK)
        );
    }

    @Test
    void hasBodyIfExists() throws Exception {
        final Storage storage = new InMemoryStorage();
        final Key key = new Key.From("repo/artifact2.jar");
        final byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        new BlockingStorage(storage).save(key, data);
        MatcherAssert.assertThat(
            new ArtifactGetResponse(storage, key),
            new RsHasBody(data)
        );
    }

    @Test
    void notFoundIfDoesnExist() {
        final Storage storage = new InMemoryStorage();
        MatcherAssert.assertThat(
            new ArtifactGetResponse(storage, new Key.From("none")),
            new RsHasStatus(RsStatus.NOT_FOUND)
        );
    }
}
