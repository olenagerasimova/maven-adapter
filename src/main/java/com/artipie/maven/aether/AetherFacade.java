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

import java.util.List;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;

/**
 * Wraps {@link ServiceLocator} and hides its boilerplate.
 * @since 0.1
 */
public final class AetherFacade implements ServiceLocator {

    /**
     * Actual ServiceLocator.
     */
    private final ServiceLocator delegate;

    /**
     * Local repository root.
     */
    private final LocalRepository repository;

    /**
     * All args constructor initializing ServiceLocator.
     * @param repository Local repository instance
     */
    public AetherFacade(final LocalRepository repository) {
        this.repository = repository;
        this.delegate = MavenRepositorySystemUtils.newServiceLocator()
            .setService(
                RepositoryConnectorFactory.class,
                BasicRepositoryConnectorFactory.class
            );
    }

    /**
     * Hides {@link RepositorySystemSession} boilerplate.
     * @return A pre-configured instance
     */
    public RepositorySystemSession newSession() {
        try {
            final var session = MavenRepositorySystemUtils.newSession();
            session.setLocalRepositoryManager(
                this.getService(LocalRepositoryManagerFactory.class)
                    .newInstance(session, this.repository)
            );
            return session;
        } catch (final NoLocalRepositoryManagerException ex) {
            throw new IllegalStateException(
                String.format("with local repository %s", this.repository.getBasedir()),
                ex
            );
        }
    }

    @Override
    public <T> T getService(final Class<T> type) {
        return this.delegate.getService(type);
    }

    @Override
    public <T> List<T> getServices(final Class<T> type) {
        return this.delegate.getServices(type);
    }
}
