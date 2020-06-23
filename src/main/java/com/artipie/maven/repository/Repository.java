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
import com.artipie.http.Response;
import java.net.URI;
import java.util.concurrent.CompletionStage;

/**
 * A Maven repository that abstracts over local, remote, etc, types of repository.
 *
 * Its responsibility is to serve GET requests for Maven artifacts.
 *
 * @since 0.5
 * @todo #92:30min Continue working on implementing a Maven proxy repository
 *  by introducing a new class Repositories that wraps an ordered list of
 *  Repository and tries them one by one to retrieve maven artifacts. Keep in mind
 *  the following points: 1) Repositories should at one point be able to serve all
 *  the related files (metadatas) of a given maven coordinates from the same Repository
 *  to avoid incoherent states, 2) Repositories should be responsible of maintaining
 *  an aggregated view of all the metadatas of the Repository it wraps, 3) There
 *  should ultimately be a configuration file as explained in #92 for instantiating
 *  Repositories. See #92 for more details on this. Don't hesitate to ask ARC about
 *  it since this todo is quite complex.
 */
public interface Repository {

    /**
     * Build a {@link Response} for a GET request to serve a Maven artifact.
     *
     * @param uri The requested artifact
     * @return Artifact data content future
     */
    CompletionStage<? extends Content> artifact(URI uri);
}
