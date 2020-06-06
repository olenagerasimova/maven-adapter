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

import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.maven.repository.Repository;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import org.reactivestreams.Publisher;

/**
 * A {@link Slice} based on a {@link Repository}. This is the main entrypoint
 * for dispatching GET requests for artifacts to the various {@link Repository}
 * implementations.
 *
 * @see Repository
 * @since 0.5
 */
public final class RepositorySlice implements Slice {

    /**
     * Maven Repository.
     */
    private final Repository repo;

    /**
     * Ctor.
     *
     * @param repo Maven Repository.
     */
    public RepositorySlice(final Repository repo) {
        this.repo = repo;
    }

    @Override
    public Response response(
        final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        return this.repo.response(new RequestLineFrom(line).uri());
    }
}
