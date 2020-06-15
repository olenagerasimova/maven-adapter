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

import com.artipie.asto.Content;
import com.artipie.asto.Storage;
import com.artipie.asto.rx.RxStorageWrapper;
import com.artipie.http.slice.KeyFromPath;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Single;
import java.net.URI;
import java.util.concurrent.CompletionStage;

/**
 * {@link Repository} getting artifacts from a local {@link Storage}.
 *
 * @since 0.5
 */
public final class RpLocal implements Repository {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Ctor.
     * @param storage Storage
     */
    public RpLocal(final Storage storage) {
        this.storage = storage;
    }

    @Override
    public CompletionStage<Content> artifact(final URI uri) {
        final KeyFromPath key = new KeyFromPath(uri.getPath());
        return Single.just(new RxStorageWrapper(this.storage)).flatMapMaybe(
            rxsto -> rxsto.exists(key)
                .filter(exists -> exists)
                .flatMapSingleElement(ignore -> rxsto.value(key))
        ).switchIfEmpty(Single.error(() -> new ArtifactNotFoundException(key)))
            .to(SingleInterop.get());
    }
}
