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

import com.artipie.asto.Storage;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.slice.SliceDownload;
import io.reactivex.Flowable;
import java.net.URI;
import java.util.Collections;

/**
 * {@link Repository} getting artifacts from a local {@link Storage}.
 *
 * @since 0.5
 */
public final class RpLocal implements Repository {

    /**
     * Delegated Slice.
     */
    private final Slice wrapped;

    /**
     * Ctor.
     * @param storage Storage
     */
    public RpLocal(final Storage storage) {
        this(new SliceDownload(storage));
    }

    /**
     * Ctor.
     * @param wrapped Delegated Slice
     */
    private RpLocal(final Slice wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Response response(final URI uri) {
        return this.wrapped.response(
            new RequestLine("GET", uri.toString(), "HTTP/1.1").toString(),
            Collections.emptySet(),
            Flowable.empty()
        );
    }
}
