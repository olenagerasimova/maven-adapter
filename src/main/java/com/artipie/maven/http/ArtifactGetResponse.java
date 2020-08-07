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
import com.artipie.http.Response;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.StandardRs;
import com.artipie.maven.repository.RepositoryChecksums;

/**
 * Artifact {@code GET} response.
 * <p>
 * It includes a body of artifact requested if exists. The code is:
 * {@code 200} if exist and {@code 404} otherwise.
 * Also, it contains artifact headers if it exits.
 * </p>
 * @see ArtifactHeaders
 * @since 0.5
 */
public final class ArtifactGetResponse extends Response.Wrap {

    /**
     * New artifact response.
     * @param storage Repository storage
     * @param location Artifact location
     */
    public ArtifactGetResponse(final Storage storage, final Key location) {
        super(
            new AsyncResponse(
                storage.exists(location).thenApply(
                    exists -> {
                        final Response rsp;
                        if (exists) {
                            rsp = new OkResponse(storage, location);
                        } else {
                            rsp = StandardRs.NOT_FOUND;
                        }
                        return rsp;
                    }
                )
            )
        );
    }

    /**
     * Ok {@code 200} response for {@code GET} request.
     * @since 0.5
     */
    private static final class OkResponse extends Response.Wrap {
        /**
         * New response.
         * @param storage Repository storage
         * @param location Artifact location
         */
        OkResponse(final Storage storage, final Key location) {
            super(
                new AsyncResponse(
                    storage.value(location).thenCombine(
                        new RepositoryChecksums(storage).checksums(location),
                        (body, checksums) ->
                            new RsWithBody(
                                new RsWithHeaders(
                                    StandardRs.OK,
                                    new ArtifactHeaders(location, checksums)
                                ),
                                body
                            )
                    )
                )
            );
        }
    }
}
