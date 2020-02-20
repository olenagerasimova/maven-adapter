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
import com.artipie.maven.ArtifactMetadata;
import com.artipie.maven.ChecksumAttribute;
import com.artipie.maven.ChecksumType;
import com.artipie.maven.DetachedMetadata;
import com.artipie.maven.FileCoordinates;
import com.artipie.maven.aether.LocalArtifactResolver;
import com.artipie.maven.aether.RemoteRepositories;
import com.artipie.maven.util.AutoCloseablePath;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.Flow;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.reactivestreams.FlowAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs deployment lifecycle in terms of Maven libraries.
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (200 lines) see {@link AetherRepository} javadoc
 */
final class Deployer {

    /**
     * Using another class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AetherRepository.class);

    /**
     * If false then the resource closing will be deferred until the stream termination.
     * @see Single#using(Callable, Function, Consumer, boolean)
     */
    private static final boolean DEFERRED_CLOSE = false;

    /**
     * Remote repositories to handle artifacts.
     */
    private final RemoteRepositories remotes;

    /**
     * Staging files root.
     */
    private final AutoCloseablePath.Parent dir;

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
     * @param dir Staging files root
     * @param repositories RepositorySystem instance
     * @param session Ongoing session
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    Deployer(
        final RemoteRepositories remotes,
        final AutoCloseablePath.Parent dir,
        final RepositorySystem repositories,
        final RepositorySystemSession session
    ) {
        this.remotes = remotes;
        this.dir = dir;
        this.repositories = repositories;
        this.session = session;
    }

    /**
     * Transfers given content to staging file and deploys it.
     * @param path Artifact URI path segment
     * @param content Artifact binary
     * @return DeployResult
     * @throws Exception Deployment failed
     */
    public Single<ArtifactMetadata> deploy(
        final String path,
        final Flow.Publisher<ByteBuffer> content
    ) {
        final var coords = new FileCoordinates(path);
        LOG.debug("deploying coords {}", coords);
        return this.staging(coords, content)
            .map(file -> new DefaultArtifact(coords.coords()).setFile(file.toFile()))
            .doOnSuccess(artifact -> this.install(artifact))
            .doOnSuccess(artifact -> this.deploy(coords, artifact))
            .map(artifact -> this.result(coords, artifact));
    }

    /**
     * Wraps subsequent operators with staging file.
     * @param coords Artifact coords
     * @param content Artifact binary
     * @return Staging file path
     */
    private Single<Path> staging(
        final FileCoordinates coords,
        final Flow.Publisher<ByteBuffer> content
    ) {
        return Single.using(
            () -> this.dir.resolve(coords.path()),
            staging -> new RxFile(staging.unwrap())
                .save(Flowable.fromPublisher(FlowAdapters.toPublisher(content)))
                .andThen(Single.defer(() -> Single.just(staging.unwrap()))),
            AutoCloseablePath::close,
            Deployer.DEFERRED_CLOSE
        );
    }

    /**
     * Installs given artifact to a local repository.
     * @param artifact An artifact to install
     * @throws InstallationException Installation failed
     */
    private void install(final Artifact artifact) throws InstallationException {
        LOG.debug("installing {}", artifact);
        this.repositories.install(
            this.session,
            new InstallRequest().addArtifact(artifact)
        );
    }

    /**
     * Deploys given artifact from a local repository to a remote repository.
     * @param coords Artifact coordinates
     * @param artifact Artifact itself
     * @throws DeploymentException If deployment failed
     */
    private void deploy(final FileCoordinates coords, final Artifact artifact)
        throws DeploymentException {
        LOG.debug("deploying {}", artifact);
        this.repositories.deploy(
            this.session,
            new DeployRequest()
                .addArtifact(artifact)
                .setRepository(this.remotes.uploading(coords))
        );
    }

    /**
     * Creates {@link ArtifactMetadata} instance.
     * @param coords Artifact coordinates
     * @param artifact Artifact instance
     * @return Resulting ArtifactMetadata
     * @throws IOException If reading from a local repository failed
     * @throws NoSuchAlgorithmException ChecksumType misconfiguration
     */
    private ArtifactMetadata result(final FileCoordinates coords, final Artifact artifact)
        throws IOException, NoSuchAlgorithmException {
        final Path file = new LocalArtifactResolver(this.session)
            .resolve(artifact);
        final var checksums = new ChecksumAttribute(file);
        return new DetachedMetadata(
            coords,
            coords.path(),
            Files.size(file),
            checksums.readHex(ChecksumType.MD5),
            checksums.readHex(ChecksumType.SHA1)
        );
    }
}
