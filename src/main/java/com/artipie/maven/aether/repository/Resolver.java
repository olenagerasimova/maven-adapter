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

package com.artipie.maven.aether.repository;

import com.artipie.asto.fs.RxFile;
import com.artipie.maven.FileCoordinates;
import com.artipie.maven.aether.RemoteRepositories;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.nio.ByteBuffer;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;

/**
 * Performs artifact resolution.
 * @since 0.1
 */
final class Resolver {

    /**
     * Remote repositories to handle artifacts.
     */
    private final RemoteRepositories remotes;

    /**
     * RepositorySystem instance.
     */
    private final RepositorySystem repositories;

    /**
     * Ongoing session.
     */
    private final RepositorySystemSession session;

    /**
     * All args constructor.
     * @param remotes Remote repositories
     * @param repositories RepositorySystem instance
     * @param session Ongoing session
     */
    Resolver(
        final RemoteRepositories remotes,
        final RepositorySystem repositories,
        final RepositorySystemSession session
    ) {
        this.remotes = remotes;
        this.repositories = repositories;
        this.session = session;
    }

    /**
     * Resolves artifact file.
     * @param path Artifact path
     * @return Artifact file
     */
    public Flowable<ByteBuffer> resolve(final String path) {
        final var coords = new FileCoordinates(path);
        return Single.fromCallable(
            () -> this.repositories.resolveArtifact(
                this.session,
                new ArtifactRequest(
                    new DefaultArtifact(coords.coords()),
                    this.remotes.downloading(coords),
                    null
                )
            )).subscribeOn(Schedulers.io())
            .map(artifact -> artifact.getArtifact().getFile().toPath())
            .map(RxFile::new)
            .flatMapPublisher(RxFile::flow);
    }
}
