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

import com.artipie.asto.Key;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.maven.http.MavenSlice;
import com.artipie.vertx.VertxSliceServer;
import com.jcabi.matchers.XhtmlMatchers;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.http.client.utils.URIBuilder;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for the not-yet implementend Maven HTTP API.
 *
 * @since 1.0
 * @todo #72:30min For some unknown reason the two tests involving
 *  the deployment of files are extremely slow, each upload of empty
 *  files takes several seconds. Fix this situation.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ParameterNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class MavenHttpITCase {

    /**
     * Vertx instance.
     */
    private Vertx vertx;

    @BeforeEach
    void setUp() {
        this.vertx = Vertx.vertx();
    }

    @AfterEach
    void tearDown() {
        this.vertx.close();
    }

    @Test
    void serveArtifact(final @TempDir Path temp) throws Exception {
        final Path remote = Files.createDirectories(temp.resolve("remote"));
        final Path artifact = Files.createDirectories(
            remote.resolve("org").resolve("apache").resolve("maven").resolve("resolver")
                .resolve("maven-resolver-util")
                .resolve("1.3.3")
        );
        Files.write(artifact.resolve("maven-resolver-util-1.3.3.jar"), new byte[]{0});
        final FileStorage storage = new FileStorage(remote);
        try (VertxSliceServer server = new VertxSliceServer(this.vertx, new MavenSlice(storage))) {
            final int port = server.start();
            MatcherAssert.assertThat(
                "Must retrieve artifact",
                new MavenArtifacts(
                    port,
                    Files.createDirectories(temp.resolve("local"))
                ).artifact("org.apache.maven.resolver:maven-resolver-util:1.3.3"),
                new IsNot<>(new IsNull<>())
            );
        }
    }

    @ParameterizedTest
    @CsvSource({"1.3.3,1", "0.1-SNAPSHOT,0"})
    void deployOneArtifact(final String version, final String cnt, final @TempDir Path temp)
        throws Exception {
        final Path jar = this.jarPath(version, temp);
        final Path pom = this.pomPath(version, temp);
        final FileStorage storage = this.prepareStorage(temp, jar, pom);
        try (VertxSliceServer server = new VertxSliceServer(this.vertx, new MavenSlice(storage))) {
            final int port = server.start();
            this.deploy(
                version, temp,
                new MavenArtifacts(port, Files.createDirectories(temp.resolve("local")))
            );
        }
        MatcherAssert.assertThat(
            new String(
                new BlockingStorage(storage).value(
                    new Key.From(
                        "org/apache/maven/resolver/maven-resolver-util/maven-metadata.xml"
                    )
                ),
                StandardCharsets.UTF_8
            ),
            XhtmlMatchers.hasXPaths(
                "metadata/groupId[text() = 'org.apache.maven.resolver']",
                "metadata/artifactId[text() = 'maven-resolver-util']",
                String.format("metadata/versioning/latest[text() = '%s']", version),
                String.format("metadata/versioning[count(//release) = %s]", cnt),
                String.format("/metadata/versioning/versions/version[text() = '%s']", version),
                "metadata/versioning/versions[count(//version) = 1]"
            )
        );
    }

    @ParameterizedTest
    @CsvSource(
        {
            "1.3.3,1.3.4,1",
            "0.1-SNAPSHOT,1.0,1",
            "0.4,0.8-SNAPSHOT,1",
            "0.8-SNAPSHOT,0.9-SNAPSHOT,0"
        }
    )
    void deployUpdateRepositoryMetadata(final String first, final String second, final String cnt,
        final @TempDir Path temp) throws Exception {
        final Path jar = this.jarPath(first, temp);
        final Path pom = this.pomPath(first, temp);
        final FileStorage storage = this.prepareStorage(temp, jar, pom);
        try (VertxSliceServer server = new VertxSliceServer(this.vertx, new MavenSlice(storage))) {
            final MavenArtifacts art = new MavenArtifacts(
                server.start(), Files.createDirectories(temp.resolve("local"))
            );
            this.deploy(first, temp, art);
            this.deploy(second, temp, art);
        }
        MatcherAssert.assertThat(
            new String(
                new BlockingStorage(storage).value(
                    new Key.From(
                        "org/apache/maven/resolver/maven-resolver-util/maven-metadata.xml"
                    )
                ),
                StandardCharsets.UTF_8
            ),
            XhtmlMatchers.hasXPaths(
                "metadata/groupId[text() = 'org.apache.maven.resolver']",
                "metadata/artifactId[text() = 'maven-resolver-util']",
                String.format("metadata/versioning/latest[text() = '%s']", second),
                String.format("metadata/versioning[count(//release) = %s]", cnt),
                String.format("/metadata/versioning/versions/version[text() = '%s']", first),
                String.format("/metadata/versioning/versions/version[text() = '%s']", second),
                "metadata/versioning/versions[count(//version) = 2]"
            )
        );
    }

    private void deploy(final String version, final Path temp, final MavenArtifacts art)
        throws DeploymentException {
        art.deploy(
            String.format("org.apache.maven.resolver:maven-resolver-util:%s", version),
            this.jarPath(version, temp), this.pomPath(version, temp)
        );
    }

    private Path jarPath(final String version, final Path temp) {
        return temp.resolve(String.format("maven-resolver-util-%s.jar", version));
    }

    private Path pomPath(final String version, final Path temp) {
        return temp.resolve(String.format("maven-resolver-util-%s.pom", version));
    }

    private FileStorage prepareStorage(final Path temp, final Path jar, final Path pom)
        throws IOException {
        Files.write(jar, new byte[]{});
        Files.write(pom, new byte[]{});
        return new FileStorage(Files.createDirectories(temp.resolve("remote")));
    }

    /**
     * Object to retrieve and upload an artifact from/to a remote maven
     * repository using the Maven tooling.
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
         * @param port The port on localhost for the remote repository.
         * @param localrepo Path to the local repository.
         * @throws URISyntaxException in case of problem.
         */
        MavenArtifacts(final int port, final Path localrepo) throws URISyntaxException {
            this(
                new RemoteRepository.Builder(
                    "artipie", "default",
                    new URIBuilder("http://localhost/")
                        .setPort(port)
                        .toString()
                ).build(),
                localrepo
            );
        }

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
            return this.system.resolveArtifact(
                this.session,
                new ArtifactRequest()
                    .setArtifact(new DefaultArtifact(coords))
                    .setRepositories(Arrays.asList(this.repository))
            ).getArtifact();
        }

        void deploy(
            final String coords, final Path jar, final Path pom
        ) throws DeploymentException {
            final Artifact jarart = new DefaultArtifact(coords)
                .setFile(jar.toFile());
            final Artifact pomart = new SubArtifact(jarart, "", "pom")
                .setFile(pom.toFile());
            this.system.deploy(
                this.session,
                new DeployRequest()
                    .setRepository(this.repository)
                    .addArtifact(jarart)
                    .addArtifact(pomart)
            );
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
