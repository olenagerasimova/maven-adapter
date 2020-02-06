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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;

/**
 * Configures {@link RepositorySystemSession}.
 * @since 0.1
 */
public class SessionFactory {

    /**
     * Local repository root.
     */
    private final LocalRepository repository;

    /**
     * Maven service locator.
     */
    private final ServiceLocator services;

    /**
     * All args constructor.
     * @param repository Local repository root
     * @param services Maven service locator
     */
    public SessionFactory(final LocalRepository repository, final ServiceLocator services) {
        this.repository = repository;
        this.services = services;
    }

    /**
     * Hides {@link RepositorySystemSession} boilerplate.
     * @return A RepositorySystemSession instance
     */
    public RepositorySystemSession newSession() {
        try {
            final var session = MavenRepositorySystemUtils.newSession();
            session.setLocalRepositoryManager(
                this.services.getService(LocalRepositoryManagerFactory.class)
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
}
