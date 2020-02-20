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
import com.artipie.maven.Repository;
import com.artipie.maven.aether.RemoteRepositories;
import com.artipie.maven.aether.ServiceLocatorFactory;
import com.artipie.maven.aether.SessionFactory;
import com.artipie.maven.util.AutoCloseablePath;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.reactivestreams.FlowAdapters;

/**
 * Maven library implementation for {@link Repository}.
 * @since 0.1
 */
public final class AetherRepository implements Repository {

    /**
     * Create request-level {@link ServiceLocator} instances.
     */
    private final ServiceLocatorFactory locators;

    /**
     * Local repository.
     */
    private final LocalRepository repository;

    /**
     * Staging files root.
     */
    private final AutoCloseablePath.Parent dir;

    /**
     * Remote repositories to handle artifacts.
     */
    private final RemoteRepositories remotes;

    /**
     * All args constructor.
     * @param locators Creates ServiceLocator instances
     * @param repository Local repository
     * @param dir Staging files root
     * @param remotes Remote repositories to handle artifacts
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public AetherRepository(
        final ServiceLocatorFactory locators,
        final LocalRepository repository,
        final AutoCloseablePath.Parent dir,
        final RemoteRepositories remotes
    ) {
        this.locators = locators;
        this.repository = repository;
        this.dir = dir;
        this.remotes = remotes;
    }

    @Override
    public Flow.Publisher<ByteBuffer> download(final String path) {
        final var locator = this.locators.serviceLocator();
        return new Resolver(
            this.remotes,
            locator.getService(RepositorySystem.class),
            new SessionFactory(this.repository, locator).newSession()
        ).resolve(path)
            .to(FlowAdapters::toFlowPublisher);
    }

    @Override
    public ArtifactMetadata upload(final String path, final InputStream content) throws Exception {
        final var locator = this.locators.serviceLocator();
        return new Deployer(
            this.remotes,
            this.dir,
            locator.getService(RepositorySystem.class),
            new SessionFactory(this.repository, locator).newSession()
        ).deploy(path, content);
    }
}
