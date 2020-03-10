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

package com.artipie.maven.aether;

import java.nio.file.Path;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;

/**
 * Resolves Maven artifacts in local repository.
 * @since 0.1
 * @deprecated Outdated due architectural changes in 0.2
 */
@Deprecated
public final class LocalArtifactResolver {

    /**
     * Ongoing {@link org.eclipse.aether.RepositorySystem} session.
     */
    private final RepositorySystemSession session;

    /**
     * All args constructor.
     * @param session Ongoing session
     */
    public LocalArtifactResolver(final RepositorySystemSession session) {
        this.session = session;
    }

    /**
     * Resolves a given artifact in local repository.
     * @param artifact The artifact to find.
     * @return Path to the artifact.
     */
    public Path resolve(final Artifact artifact) {
        if (artifact == null) {
            throw new IllegalArgumentException("artifact should not be null");
        }
        final var lrm = this.session.getLocalRepositoryManager();
        if (lrm == null) {
            throw new IllegalStateException("LocalRepositoryManager should not be null");
        }
        return lrm.getRepository().getBasedir().toPath().resolve(
            lrm.getPathForLocalArtifact(artifact)
        );
    }
}
