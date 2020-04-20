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
package com.artipie.maven;

import java.nio.file.Path;
import java.util.Arrays;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the not-yet implementend Maven HTTP API.
 *
 * @since 1.0
 * @todo #71:30min Continue working on the Maven HTTP API for downloading artifacts: we need to 1)
 *  add to the test serveArtifact a) the instantiation of the artipie Maven server with a test
 *  artifact being served via the Maven HTTP API (see README for details) and b) configure the
 *  remote repository in the test serveArtifact to point to the Artipie Maven server and c) verify
 *  the test serveArtifact is able to download the artifact correctly via aether (already setup
 *  in test serveArtifact). Once this is done, then 2) implement those HTTP API so that the test
 *  serveArtifact passes.
 */
public final class MavenHttpITCase {

    @Test
    public void serveArtifact(final @TempDir Path localrepo) throws Exception {
        MatcherAssert.assertThat(
            "Must retrieve artifact",
            new MavenArtifacts(
                new RemoteRepository.Builder(
                    "central", "default",
                    "https://repo1.maven.org/maven2/"
                ).build(),
                localrepo
            ).artifact("org.apache.maven.resolver:maven-resolver-util:1.3.3"),
            new IsNot<>(new IsNull<>())
        );
    }

    /**
     * Object to retrieve an artifact from a remote maven
     * repository using the Maven tooling into a temporary
     * local repository.
     *
     * @since 1.0
     */
    public static final class MavenArtifacts {

        /**
         * The system.
         */
        private final RepositorySystem system;

        /**
         * The session.
         */
        private final RepositorySystemSession session;

        /**
         * The remote repository.
         */
        private final RemoteRepository repository;

        /**
         * Ctor.
         *
         * @param repository The repository.
         * @param localrepo Path to the local repository.
         */
        MavenArtifacts(final RemoteRepository repository, final Path localrepo) {
            this.repository = repository;
            this.system = newRepositorySystem();
            this.session = newRepositorySystemSession(
                this.system,
                new LocalRepository(localrepo.toFile())
            );
        }

        /**
         * Retrieve an {@link Artifact}.
         *
         * @param coords The maven coordinates.
         * @return The corresponding artifact.
         * @throws ArtifactResolutionException If there is an error.
         */
        Artifact artifact(final String coords) throws ArtifactResolutionException {
            final ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(new DefaultArtifact(coords));
            request.setRepositories(Arrays.asList(this.repository));
            return this.system.resolveArtifact(
                this.session, request
            ).getArtifact();
        }

        private static RepositorySystem newRepositorySystem() {
            final DefaultServiceLocator locator =
                MavenRepositorySystemUtils.newServiceLocator();
            locator.addService(
                RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class
            );
            locator.addService(
                TransporterFactory.class,
                FileTransporterFactory.class
            );
            locator.addService(
                TransporterFactory.class,
                HttpTransporterFactory.class
            );
            return locator.getService(RepositorySystem.class);
        }

        private static RepositorySystemSession newRepositorySystemSession(
            final RepositorySystem system,
            final LocalRepository repository
        ) {
            final DefaultRepositorySystemSession session =
                MavenRepositorySystemUtils.newSession();
            session.setLocalRepositoryManager(
                system.newLocalRepositoryManager(session, repository)
            );
            return session;
        }
    }
}
