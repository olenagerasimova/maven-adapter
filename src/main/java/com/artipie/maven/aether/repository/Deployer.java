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

import com.artipie.maven.ArtifactMetadata;
import com.artipie.maven.ChecksumAttribute;
import com.artipie.maven.ChecksumType;
import com.artipie.maven.DetachedMetadata;
import com.artipie.maven.FileCoordinates;
import com.artipie.maven.aether.LocalArtifactResolver;
import com.artipie.maven.aether.RemoteRepositories;
import com.artipie.maven.util.AutoCloseablePath;
import com.artipie.maven.util.FileCleanupException;
import com.google.common.collect.Iterables;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.installation.InstallRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs deployment lifecycle in terms of Maven libraries.
 * @since 0.1
 */
final class Deployer {

    /**
     * Using another class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AetherRepository.class);

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
    public ArtifactMetadata deploy(
        final String path,
        final InputStream content
    ) throws Exception {
        final var coords = new FileCoordinates(path);
        DeployResult result = null;
        try (var staging = this.dir.resolve(path)) {
            try (var file = Files.newOutputStream(staging.unwrap())) {
                content.transferTo(file);
            }
            final var artifact = new DefaultArtifact(
                coords.coords()
            ).setFile(
                staging.unwrap().toFile()
            );
            this.repositories.install(
                this.session,
                new InstallRequest().addArtifact(artifact)
            );
            result = this.repositories.deploy(
                this.session,
                new DeployRequest()
                    .addArtifact(artifact)
                    .setRepository(
                        this.remotes.uploading(coords)
                    )
            );
        } catch (final FileCleanupException ex) {
            LOG.warn("on AutoCloseablePath cleanup", ex);
        }
        final Artifact deployed = Iterables.getOnlyElement(
            result.getArtifacts()
        );
        final Path file = new LocalArtifactResolver(this.session)
            .resolve(deployed);
        final var checksums = new ChecksumAttribute(file);
        return new DetachedMetadata(
            coords,
            path,
            Files.size(file),
            checksums.readHex(ChecksumType.MD5),
            checksums.readHex(ChecksumType.SHA1)
        );
    }
}
